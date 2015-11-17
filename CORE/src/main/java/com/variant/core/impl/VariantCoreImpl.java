package com.variant.core.impl;

import static com.variant.core.schema.parser.MessageTemplate.BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.schema.parser.MessageTemplate.BOOT_CONFIG_FILE_NOT_FOUND;
import static com.variant.core.schema.parser.MessageTemplate.BOOT_CONFIG_RESOURCE_NOT_FOUND;
import static com.variant.core.schema.parser.MessageTemplate.BOOT_EVENT_PERSISTER_NO_INTERFACE;
import static com.variant.core.schema.parser.MessageTemplate.BOOT_TARGETING_PERSISTER_NO_INTERFACE;
import static com.variant.core.schema.parser.MessageTemplate.INTERNAL;
import static com.variant.core.schema.parser.MessageTemplate.RUN_PROPERTY_NOT_SET;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.flashpoint.Flashpoint;
import com.variant.core.flashpoint.FlashpointListener;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.impl.ParserResponseImpl;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.schema.parser.Severity;
import com.variant.core.session.SessionService;
import com.variant.core.session.TargetingPersister;
import com.variant.core.util.VariantIoUtils;

/**
 * The Variant CORE API.
 * 
 * @author Igor
 *
 */
public class VariantCoreImpl implements Variant {

	private static final Logger LOG = LoggerFactory.getLogger(VariantCoreImpl.class);
	
	private boolean isBootstrapped = false;
	private Schema schema = null;
	private EventWriter eventWriter = null;
	private SessionService sessionService = null;
	private Flasher flasher = new Flasher();
	
	private static String version() {
		String version = RuntimeService.getVersion();
		if (version == null) version = "?";
		return "V. " + version + " (Alpha), Copyright (C) 2015 getvariant.com";
	}

	/**
	 * 
	 */
	private void stateCheck() {
		if (!isBootstrapped) throw new IllegalStateException("Variant must be bootstrapped first by calling one of the bootstrap() methods");
	}
		
	/**
	 * Setup system properties.
	 * 3. Process command line args.
	 * 
	 * @param resourceName
	 */
	private void setupSystemProperties(String...resourceNames) {

		// Override system props in left-to-right scan.
		for (int i = resourceNames.length - 1; i >= 0; i--) {
			String name = resourceNames[i];
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream(name));
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read resurce [" + name + "]");
			}
		}
		
		// Override with /variant.props if supplied on classpath.
		try {
			VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream("/variant.props"));
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processed application properties resource file [/variant.props]]");
			}
		}
		catch (Exception e) {}  // Not an error if wasn't found.

		String runTimePropsResourceName = System.getProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME);
		String runTimePropsFileName = System.getProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME);
		
		if (runTimePropsResourceName != null && runTimePropsFileName!= null) {
			throw new VariantRuntimeException(BOOT_CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);
		}
		
		if (runTimePropsResourceName != null) {
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream(runTimePropsResourceName));
			}
			catch (Exception e) {
				throw new VariantRuntimeException(BOOT_CONFIG_RESOURCE_NOT_FOUND, e, runTimePropsResourceName);
			}			
		}
		else if (runTimePropsFileName != null) {
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openFileAsStream(runTimePropsFileName));
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
	@Override
	public synchronized void bootstrap(String...resourceNames) {
		

		if (isBootstrapped) throw new IllegalStateException("Variant is already bootstrapped");

		long now = System.currentTimeMillis();

		setupSystemProperties(resourceNames);
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("*** Do not use on a production system ***");
			LOG.debug("Bootstrapping Variant with following system properties:");
			for (VariantProperties.Keys key: VariantProperties.Keys.values()) {
				LOG.debug("  " + key.propName() + " = " + key.propValue());
			}
		}
		
		//
		// Instantiate event persister.
		//
		String eventPersisterClassName = VariantProperties.getInstance().eventPersisterClassName();
		if (eventPersisterClassName == null) {
			throw new VariantRuntimeException(RUN_PROPERTY_NOT_SET, VariantProperties.Keys.EVENT_PERSISTER_CLASS_NAME.propName());
		}
		
		EventPersister eventPersister = null;
		try {
			Object eventPersisterObject = Class.forName(eventPersisterClassName).newInstance();
			if (eventPersisterObject instanceof EventPersister) {
				eventPersister = (EventPersister) eventPersisterObject;
			}
			else {
				throw new VariantRuntimeException (BOOT_EVENT_PERSISTER_NO_INTERFACE, eventPersisterClassName, EventPersister.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException(
					"Unable to instantiate event persister class [" + VariantProperties.getInstance().eventPersisterClassName() +"]", e);
		}
				
		// Instantiate event writer.
		eventWriter = new EventWriter(eventPersister);
		
		// Pass the config to the new object.
		eventPersister.initialized();
		
		//
		// Instantiate session service.
		//
		sessionService = new SessionService();
				
		isBootstrapped = true;
		
		LOG.info(
				String.format("Core %s bootstrapped in %s",
						version(),
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
	}
	
	/**
	 * 
	 */
	@Override
	public boolean isBootstrapped() {
		return isBootstrapped;
	}
	
	/**
	 * 
	 */
	@Override
	public synchronized void shutdown() {
		long now = System.currentTimeMillis();
		stateCheck();
		isBootstrapped = false;
		schema = null;
		flasher.clear();
		eventWriter.shutdown();
		eventWriter = null;
		sessionService.shutdown();
		sessionService = null;
		LOG.info(
				String.format("Core %s shutdown in %s",
						version(),
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
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
	 * @param string
	 * @param deploy
	 * @return
	 */
	@Override
	public ParserResponse parseSchema(String string, boolean deploy) {

		stateCheck();
		
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
			response = SchemaParser.parse(schemaAsStringNoComments.toString());
		}
		catch (Throwable t) {
			response = new ParserResponseImpl();
			ParserMessage err = response.addMessage(INTERNAL, t.getMessage());
			LOG.error(err.getMessage(), t);
		}

		// Only replace the schema if no ERROR or higher level errors.
		if (response.highestMessageSeverity().lessThan(Severity.ERROR)) {
			
			if (schema != null) {
				((SchemaImpl)schema).setInternalState(SchemaImpl.InternalState.UNDEPLOYED);
			}
			schema = response.getSchema();
			((SchemaImpl)schema).setInternalState(SchemaImpl.InternalState.DEPLOYED);
			
			StringBuilder msg = new StringBuilder("New schema deployed in ");
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
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream) {

		return parseSchema(stream, true);
	}

	/**
	 * 
	 */
	@Override
	public ParserResponse parseSchema(String string) {

		return parseSchema(string, true);
	}

	/**
	 * 
	 */
	@Override
	public Schema getSchema() {

		stateCheck();
		return schema;
	
	}
		
	/**
	 * 
	 */
	@Override
	public VariantSession getSession(Object sessionIdPersisterUserData) {
		return sessionService.getSession(true, sessionIdPersisterUserData);
	}

	/**
	 * 
	 */
	@Override
	public VariantStateRequest newStateRequest(VariantSession session, State view, Object targetingPersisterUserData) {
		
		stateCheck();
		
		// init Targeting Persister with the same user data.
		TargetingPersister tp = null;
		String className = VariantProperties.getInstance().targetingPersisterClassName();
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof TargetingPersister) {
				tp = (TargetingPersister) object;
			}
			else {
				throw new VariantBootstrapException(BOOT_TARGETING_PERSISTER_NO_INTERFACE, className, TargetingPersister.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate targeting persister class [" + className +"]", e);
		}
			
		tp.initialized(session, targetingPersisterUserData);

		return VariantRuntime.startViewRequest(session, view, tp);
	}
	
	/**
	 * 
	 */
	@Override
	public void commitStateRequest(VariantStateRequest request, Object userData) {

		stateCheck();
		
		if (((VariantStateRequestImpl)request).isCommitted()) {
			throw new IllegalStateException("Request already committed");
		}
		
		// Persist session Id.
		sessionService.persistSessionId(request.getSession(), userData);
		
		// Persist targeting info.  Note that we expect the userData to apply to both!
		request.getTargetingPersister().persist(userData);
		
		// Save the state serve event, if any. There may not be any if we hit a known state that did not have
		// any tests instrumented on it.
		StateServeEvent event = request.getStateServeEvent();
		if (event != null) {
			EventWriter ew = ((VariantCoreImpl) Variant.Factory.getInstance()).getEventWriter();
			ew.write(event);		
		}
		
		((VariantStateRequestImpl)request).commit();
		
	}
	
	@Override
	public void addFlashpointListener(FlashpointListener<? extends Flashpoint> listener) {
		stateCheck();
		if (listener == null) throw new IllegalArgumentException("Argument cannot be null");
		flasher.addListener(listener);		
	}

	@Override
	public void clearFlashpointListeners() {
		stateCheck();
		flasher.clear();		
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * 
	 * @return
	 */
	public EventWriter getEventWriter() {
		stateCheck();
		return eventWriter;
	}
	
	/**
	 * 
	 * @return
	 */
	public Flasher getFlasher() {
		return flasher;
	}
}
