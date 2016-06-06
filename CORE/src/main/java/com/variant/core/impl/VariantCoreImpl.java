package com.variant.core.impl;

import static com.variant.core.schema.impl.MessageTemplate.BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_CONFIG_FILE_NOT_FOUND;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_CONFIG_RESOURCE_NOT_FOUND;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_EVENT_PERSISTER_NO_INTERFACE;
import static com.variant.core.schema.impl.MessageTemplate.INTERNAL;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.VariantProperties.Key;
import com.variant.core.VariantSession;
import com.variant.core.event.EventPersister;
import com.variant.core.event.impl.EventWriter;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.hook.HookListener;
import com.variant.core.hook.UserHook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.ParserResponseImpl;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
import com.variant.core.session.SessionService;
import com.variant.core.util.VariantIoUtils;

/**
 * The Variant CORE API.
 * 
 * @author Igor
 *
 */
public class VariantCoreImpl implements Variant, Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(VariantCoreImpl.class);
	
	private Schema schema = null;
	private EventWriter eventWriter = null;
	private SessionService sessionService = null;
	private UserHooker hooker = new UserHooker();
	private VariantPropertiesImpl properties;
	private VariantRuntime runtime;
	private VariantComptime comptime;
		
	/**
	 * Setup system properties.
	 * 3. Process command line args.
	 * 
	 * @param resourceName
	 */
	private void setupSystemProperties(String...resourceNames) {

		properties = new VariantPropertiesImpl(this);

		// Override system props in left-to-right scan.
		for (int i = resourceNames.length - 1; i >= 0; i--) {
			String name = resourceNames[i];
			try {
				properties.overrideWith(VariantIoUtils.openResourceAsStream(name), name);
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read resurce [" + name + "]", e);
			}
		}
		
		// Override with /variant.props if supplied on classpath.
		try {
			properties.overrideWith(VariantIoUtils.openResourceAsStream("/variant.props"), "/variant.props");
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processed application properties resource file [/variant.props]]");
			}
		}
		catch (Exception e) {} // Not an error if wasn't found.

		String runTimePropsResourceName = System.getProperty(VariantProperties.COMMANDLINE_RESOURCE_NAME);
		String runTimePropsFileName = System.getProperty(VariantPropertiesImpl.COMMANDLINE_FILE_NAME);
		
		if (runTimePropsResourceName != null && runTimePropsFileName!= null) {
			throw new VariantRuntimeException(BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);
		}
		
		if (runTimePropsResourceName != null) {
			try {
				properties.overrideWith(
						VariantIoUtils.openResourceAsStream(runTimePropsResourceName), 
						"-D" + VariantPropertiesImpl.COMMANDLINE_RESOURCE_NAME + "=" + runTimePropsResourceName);
			}
			catch (Exception e) {
				throw new VariantRuntimeException(BOOT_CONFIG_RESOURCE_NOT_FOUND, e, runTimePropsResourceName);
			}
		}
		else if (runTimePropsFileName != null) {
			try {
				properties.overrideWith(
						VariantIoUtils.openFileAsStream(runTimePropsFileName),
						 "-D" + VariantPropertiesImpl.COMMANDLINE_FILE_NAME + "=" + runTimePropsFileName);
			}
			catch (Exception e) {
				throw new VariantRuntimeException(BOOT_CONFIG_FILE_NOT_FOUND, e, runTimePropsFileName);
			}			
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	/**
	 * 
	 */
	public VariantCoreImpl(String...resourceNames) throws Exception {
		
		//
		// System properties first.
		//
		setupSystemProperties(resourceNames);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("+-- Bootstrapping Variant with following application properties: --");
			for (VariantPropertiesImpl.Key key: VariantPropertiesImpl.Key.values()) {
				LOG.debug("| " + key.propName() + " = " + properties.get(key, String.class) + " : " + properties.getSource(key));
			}
			LOG.debug("+------------- Fingers crossed, this is not PRODUCTION -------------");
		}
		
		//
		// Init comptime service. 
		//
		comptime = new VariantComptime();

		//
		// Instantiate event persister.
		//
		String eventPersisterClassName = properties.get(Key.EVENT_PERSISTER_CLASS_NAME, String.class);
		
		EventPersister eventPersister = null;
		try {
			Object eventPersisterObject = Class.forName(eventPersisterClassName).newInstance();
			if (eventPersisterObject instanceof EventPersister) {
				eventPersister = (EventPersister) eventPersisterObject;
				eventPersister.initialized(properties.get(Key.EVENT_PERSISTER_CLASS_INIT, InitializationParams.class));
			}
			else {
				throw new VariantRuntimeException (BOOT_EVENT_PERSISTER_NO_INTERFACE, eventPersisterClassName, EventPersister.class.getName());
			}
		}
		catch (VariantRuntimeException e) {
			throw e;
		}
		catch (Exception e) {
			throw new VariantInternalException(
					"Unable to instantiate event persister class [" + eventPersisterClassName +"]", e);
		}
				
		// Instantiate event writer.
		eventWriter = new EventWriter(eventPersister, properties);
				
		//
		// Instantiate session service.
		//
		sessionService = new SessionService(this);

		//
		// Instantiate runtime.
		//
		runtime = new VariantRuntime(this);

	}
	
	/**
	 * 
	 */
	@Override
	public VariantProperties getProperties() {
		return properties;
	}

	/**
	 *	 
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {
		
		String input = null;
		try {
			input = IOUtils.toString(stream);
		} catch (IOException e) {
			throw new VariantInternalException("Unable to read input from stream", e);
		}
		return parseSchema(input, deploy);
	}


	/**
	 * 
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream) {

		return parseSchema(stream, true);
	}

	/**
	 * 
	 */
	@Override
	public Schema getSchema() {
		return schema;	
	}
		
	/**
	 * 
	 */
	@Override
	public VariantSession getSession(Object...userData) {
		return sessionService.getSession(userData);
	}

		
	@Override
	public void addHookListener(HookListener<? extends UserHook> listener) {
		if (listener == null) throw new IllegalArgumentException("Argument cannot be null");
		hooker.addListener(listener);		
	}

	@Override
	public void clearHookListeners() {
		hooker.clear();		
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	/**
	 * Expose runtime to tests via package visible getter.
	 * @return
	 */
	public VariantRuntime getRuntime() {
		return runtime;
	}

	/** 
	 * @return
	 */
	public SessionService getSessionService() {
		return sessionService;
	}

	/**
	 * 
	 */
	public ParserResponse parseSchema(String schema) {
		
		return parseSchema(schema, true);
	}

	/**
	 * 
	 * @param string
	 * @param deploy
	 * @return
	 */
	public ParserResponse parseSchema(String string, boolean deploy) {

		long now = System.currentTimeMillis();
		
		// (Re)discover and process all annotations.
		// AnnotationProcessor.process();

		ParserResponseImpl response;

		// Lose comments, i.e. from // to eol.
		StringBuilder schemaAsStringNoComments = new StringBuilder();
		String lines[] = string.split("\n");
		for (String line: lines) {
			
			int commentIndex = line.indexOf("//");
			
			if (commentIndex == 0) {
				// full line - skip.
				continue;
			}
			else if (commentIndex >= 0) {
				// partial line: remove from // to eol.
				schemaAsStringNoComments.append(line.substring(0, commentIndex));
			}
			else {
				schemaAsStringNoComments.append(line);
			}
			schemaAsStringNoComments.append("\n");
		}
		
		try {
			response = SchemaParser.parse(this, schemaAsStringNoComments.toString());
		}
		catch (Throwable t) {
			response = new ParserResponseImpl();
			ParserMessage err = response.addMessage(INTERNAL, t.getMessage());
			LOG.error(err.getText(), t);
		}

		// Only replace the schema if no ERROR or higher level errors.
		Severity highSeverity = response.highestMessageSeverity();
		if (highSeverity == null || highSeverity.lessThan(Severity.ERROR)) {
			
			if (schema != null) {
				((SchemaImpl)schema).setInternalState(SchemaImpl.InternalState.UNDEPLOYED);
			}
			schema = response.getSchema();
			((SchemaImpl)schema).setInternalState(SchemaImpl.InternalState.DEPLOYED);
			
			StringBuilder msg = new StringBuilder();
			msg.append("New schema ID [").append(schema.getId()).append("] deployed in ");
			msg.append(DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")).append(":");
			for (Test test: schema.getTests()) {
				msg.append("\n   ").append(test.getName()).append(" {");
				boolean first = true;
				for (Experience exp: test.getExperiences()) {
					if (first) first = false;
					else msg.append(", ");
					msg.append(exp.getName());
					if (exp.isControl()) msg.append(" (control)");
				}
				msg.append("}");
				if (!test.isOn()) msg.append(" OFF");
			}
			LOG.info(msg.toString());
		}
		else {
			((SchemaImpl) response.getSchema()).setInternalState(SchemaImpl.InternalState.FAILED);
			LOG.error("New schema was not deployed due to parser error(s).");
		}
		
		return response;
	}

	/**
	 * 
	 * @return
	 */
	public EventWriter getEventWriter() {
		return eventWriter;
	}
	
	/**
	 * 
	 * @return
	 */
	public UserHooker getUserHooker() {
		return hooker;
	}
	
	/**
	 * Shutdown event writer when garbage collected
	 * to prevent async thread leak.
	 */
	@Override
	public void finalize() {
		eventWriter.shutdown();
	}
	
	/**
	 * 
	 * @return
	 */
	public VariantComptime getComptime() {
		return comptime;
	}
	
	/**
	 */
	private static final long serialVersionUID = 1L;

}
