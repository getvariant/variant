package com.variant.core.impl;

import static com.variant.core.error.ErrorTemplate.BOOT_EVENT_PERSISTER_NO_INTERFACE;
import static com.variant.core.error.ErrorTemplate.INTERNAL;
import static com.variant.core.error.ErrorTemplate.RUN_PROPERTY_NOT_SET;
import static com.variant.core.error.ErrorTemplate.RUN_TP_NOT_INITIALIZED;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantRuntimeException;
import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.error.Severity;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.ParserResponseImpl;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.session.SessionService;
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
	 * 
	 * 1. if argument not null, override the defautl with it.
	 * 2. Look for props as a resource
	 * 3. Look for props as a file
	 * 
	 * @param resourceName
	 */
	private void setupSystemProperties(String resourceName) {

		if (resourceName != null) {
			System.out.println(resourceName);
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream(resourceName));
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read resurce [" + resourceName + "]");
			}
		}
		
		String runTimePropsResourceName = System.getProperty(VariantProperties.RUNTIME_PROPS_RESOURCE_NAME);
		String runTimePropsFileName = System.getProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME);
		if (runTimePropsResourceName != null) {
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream(runTimePropsResourceName));
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read resurce [" + runTimePropsResourceName + "]", e);
			}			
		}
		else if (runTimePropsFileName != null) {
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openFileAsStream(runTimePropsFileName));
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read file [" + runTimePropsFileName + "]", e);
			}			
		}
		else {
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream("/variant.props"));
			}
			catch (Exception e) {}  // Not an error if wasn't found.
		}
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	public synchronized void bootstrap() {
		bootstrap(null);
	}

	/**
	 * 
	 */
	@Override
	public synchronized void bootstrap(String resourceName) {
		

		if (isBootstrapped) throw new IllegalStateException("Variant is already bootstrapped");

		long now = System.currentTimeMillis();

		setupSystemProperties(resourceName);
		
		/* Don't -- puts password in the log.
		if (LOG.isDebugEnabled()) {
			LOG.debug("Bootstrapping Variant with following system properties:");
			for (VariantProperties.Keys key: VariantProperties.Keys.values()) {
				LOG.debug("  " + key.propName() + " = " + key.propValue());
			}
		}
		*/
		
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
	public synchronized void shutdown() {
		long now = System.currentTimeMillis();
		stateCheck();
		isBootstrapped = false;
		schema = null;
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
			LOG.error(t.getMessage(), t);
			response = new ParserResponseImpl();
			response.addError(INTERNAL, t.getMessage() + ". See log for details.");
		}

		// Only replace the schema if no ERROR or higher level errors.
		if (response.highestErrorSeverity().lessThan(Severity.ERROR)) {
			schema = response.getSchema();
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
	public VariantSession getSession(boolean create, Object userData) {
		return sessionService.getSession(create, userData);
	}
	
	/**
	 * 
	 */
	@Override
	public VariantSession getSession(Object userData) {
		return sessionService.getSession(true, userData);
	}

	/**
	 * 
	 */
	@Override
	public VariantViewRequest startViewRequest(VariantSession session, View view) {
		
		stateCheck();
		// It's caller's responsibility to init the targeting persister.
		if (session.getTargetingPersister() == null) {
			throw new VariantRuntimeException(RUN_TP_NOT_INITIALIZED);
		}
		return VariantRuntime.startViewRequest(session, view);
	}
	
	/**
	 * 
	 */
	@Override
	public void commitViewRequest(VariantViewRequest request, Object userData) {

		stateCheck();
		
		if (((VariantViewRequestImpl)request).isCommitted()) {
			throw new IllegalStateException("Request already committed");
		}
		
		// Persist session Id.
		sessionService.persistSessionId(request.getSession(), userData);
		
		// Persist targeting info.  Note that we expect the userData to apply to both!
		request.getSession().getTargetingPersister().persist(userData);
		
		// Save the events.
		EventWriter ew = ((VariantCoreImpl) Variant.Factory.getInstance()).getEventWriter();
		ew.write(request.getViewServeEvent());		
		((VariantViewRequestImpl)request).commit();
		
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
	
}
