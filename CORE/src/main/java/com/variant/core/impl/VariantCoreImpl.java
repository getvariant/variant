package com.variant.core.impl;

import static com.variant.core.error.ErrorTemplate.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.ParserResponse;
import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantViewRequest;
import com.variant.core.error.Severity;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.TestParsedEventListener;
import com.variant.core.schema.View;
import com.variant.core.schema.ViewParsedEventListener;
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
	private ArrayList<TestParsedEventListener> testParsedEventListeners = new ArrayList<TestParsedEventListener>();
	private ArrayList<ViewParsedEventListener> viewParsedEventListeners = new ArrayList<ViewParsedEventListener>();
			
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
	public synchronized void shutdown() {
		long now = System.currentTimeMillis();
		stateCheck();
		isBootstrapped = false;
		schema = null;
		viewParsedEventListeners.clear();
		testParsedEventListeners.clear();
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
		AnnotationProcessor.process();

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
			response = SchemaParser.parse(schemaAsStringNoComments.toString(), viewParsedEventListeners, testParsedEventListeners);
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
	
	@Override
	public void addListener(TestParsedEventListener listener) {
		stateCheck();		
		if (listener == null) throw new IllegalArgumentException("Argument cannot be null");
		testParsedEventListeners.add(listener);
	}

	@Override
	public void addListener(ViewParsedEventListener listener) {
		stateCheck();		
		if (listener == null) throw new IllegalArgumentException("Argument cannot be null");
		viewParsedEventListeners.add(listener);
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
