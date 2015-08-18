package com.variant.core;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.error.ErrorTemplate;
import com.variant.core.error.Severity;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.runtime.RuntimeService;
import com.variant.core.runtime.VariantRuntime;
import com.variant.core.runtime.VariantViewRequestImpl;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.ParserResponseImpl;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.session.SessionKeyResolver;
import com.variant.core.session.SessionService;
import com.variant.core.util.VariantIoUtils;

/**
 * The Variant Container.
 * 
 * @author Igor
 *
 */
public class Variant {

	private static Logger logger = LoggerFactory.getLogger("Variant");
	private static boolean isBootstrapped = false;
	private static Schema schema = null;
	private static EventWriter eventWriter = null;
	private static SessionService sessionService = null;
	
	// Tests will call this to replace logger implementation.
	static void setLogger(Logger logger) {
		Variant.logger = logger;
	}
	
	/**
	 * Static singleton.
	 */
	private Variant() {
		throw new RuntimeException("Don't call us.");
	}
	
	/**
	 * 
	 */
	private static void stateCheck() {
		if (!isBootstrapped) throw new IllegalStateException("Variant must be bootstrapped first by calling one of the bootstrap() methods");
	}
	
	private static String version() {
		String version = RuntimeService.getVersion();
		if (version == null) version = "?";
		return "V. " + version + " (Alpha), Copyright (C) 2015 getvariant.com";
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
	private static void setupSystemProperties(String resourceName) {

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
		if (runTimePropsResourceName != null) {
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openResourceAsStream(runTimePropsResourceName));
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read resurce [" + runTimePropsResourceName + "]", e);
			}			
		}
		
		String runTimePropsFileName = System.getProperty(VariantProperties.RUNTIME_PROPS_FILE_NAME);
		if (runTimePropsFileName != null) {
			try {
				VariantProperties.getInstance().override(VariantIoUtils.openFileAsStream(runTimePropsFileName));
			}
			catch (Exception e) {
				throw new RuntimeException("Unable to read file [" + runTimePropsFileName + "]", e);
			}			
		}

	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Container bootstrap with standard configuration semantics:
	 * 1. Defaults
	 * 2. Command line file.
	 * 3. Command line resource.
	 * 4. Conventional resource.
	 * 
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public static synchronized void bootstrap() throws VariantBootstrapException {
		bootstrap(null);
	}
	
	/**
	 * Container bootstrap with default override configuration semantics:
	 * 1. Defaults
	 * 2. Values from the config argument override the defaults.
	 * 3. Command line file.
	 * 4. Command line resource.
	 * 5. Conventional resource.
	 * 
	 * @param config
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static synchronized void bootstrap(String resourceName) throws VariantBootstrapException {
		

		if (isBootstrapped) throw new IllegalStateException("Variant is already bootstrapped");

		long now = System.currentTimeMillis();

		setupSystemProperties(resourceName);
		
		if (logger.isDebugEnabled()) {
			logger.debug("Bootstrapping Variant with following system properties:");
			for (VariantProperties.Keys key: VariantProperties.Keys.values()) {
				logger.debug("  " + key.propName() + " = " + key.propValue());
			}
		}
			
		//
		// Instantiate event persister.
		//
		String eventPersisterClassName = VariantProperties.getInstance().eventPersisterClassName();
		if (eventPersisterClassName == null) {
			throw new VariantRuntimeException(ErrorTemplate.RUN_PROPERTY_NOT_SET, VariantProperties.Keys.EVENT_PERSISTER_CLASS_NAME.propName());
		}
		
		EventPersister eventPersister = null;
		try {
			Object eventPersisterObject = Class.forName(eventPersisterClassName).newInstance();
			if (eventPersisterObject instanceof EventPersister) {
				eventPersister = (EventPersister) eventPersisterObject;
			}
			else {
				throw new VariantBootstrapException(
						"Event persister class [" + eventPersisterClassName + "] must implement interface [" + EventPersister.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantBootstrapException(
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
		
		logger.info(
				String.format("Core %s bootstrapped in %s",
						version(),
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
	}
	
	/**
	 * Programmatically shutdown Variant container.
	 */
	public static synchronized void shutdown() {
		long now = System.currentTimeMillis();
		stateCheck();
		isBootstrapped = false;
		schema = null;
		eventWriter.shutdown();
		eventWriter = null;
		sessionService.shutdown();
		sessionService = null;
		Variant.getLogger().info(
				String.format("Core %s shutdown in %s",
						version(),
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
	}
	
	/**
	 * Parse and, if no errors, optionally deploy a new schema.
	 * @param schemaAsString
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public static ParserResponse parseSchema(InputStream schemaAsInputString, boolean deploy) {
		
		String input = null;
		try {
			input = IOUtils.toString(schemaAsInputString);
		} catch (IOException e) {
			throw new VariantInternalException("Unable to read input from stream", e);
		}
		return parseSchema(input, deploy);
	}


	/**
	 * Parse and, if no errors, optionally deploy a new schema.
	 * @param schemaAsString
	 * @deploy The new test schema will be deployed if this is true and no parse errors were encountered.
	 * @return
	 */
	public static ParserResponse parseSchema(String schemaAsString, boolean deploy) {

		stateCheck();
		long now = System.currentTimeMillis();
		ParserResponseImpl response;

		// Lose comments, i.e. from // to eol.
		StringBuilder schemaAsStringNoComments = new StringBuilder();
		String lines[] = schemaAsString.split("\n");
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
			Variant.getLogger().error(t.getMessage(), t);
			response = new ParserResponseImpl();
			response.addError(ErrorTemplate.INTERNAL, t.getMessage() + ". See log for details.");
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
			logger.info(msg.toString());
		}
		else {
			logger.error("New schema was not deployed due to parser error(s).");
		}
		
		return response;
	}
	
	/**
	 * Parse and, if no errors, deploy a new schema. The new config will not be deployed if parse errors were encountered.
	 * @param configAsString
	 * @return
	 */
	public static ParserResponse parseSchema(InputStream schemaAsInputStream) {

		return parseSchema(schemaAsInputStream, true);
	}

	/**
	 * Parse and, if no errors, deploy a new schema. The new config will not be deployed if parse errors were encountered.
	 * @param configAsString
	 * @return
	 */
	public static ParserResponse parseSchema(String schemaAsString) {

		return parseSchema(schemaAsString, true);
	}

	/**
	 * Get current configuration.
	 * @return Current test configuration or null, if none has been deployed yet.
	 */
	public static Schema getSchema() {

		stateCheck();
		return schema;
	
	}
	
	/**
	 *  Get user's Variant session. 
	 *  
	 * @param create   Whether or not create the session if does not exist.
	 * @param userData Client code can supply runtime details that will be passed without
	 *                 interpretation to <code>SessionKeyResolver.getSessionKey()</code>
	 *                 In an environment like servlet container, this will be http request.
	 * @return          
	 * @throws VariantRuntimeException 
	 */
	public static VariantSession getSession(boolean create, SessionKeyResolver.UserData userData) {
		return sessionService.getSession(create, userData);
	}
	
	/**
	 * Get user's Variant session. 
	 * 
	 * @param userArgs Client code can supply runtime details that will be passed without
	 *                 interpretation to <code>SessionKeyResolver.getSessionKey()</code>
	 *                 In an environment like servlet container, this will be http request.
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public static VariantSession getSession(SessionKeyResolver.UserData userData) {
		return sessionService.getSession(true, userData);
	}

	/**
     * Start view Request 
	 * @return
	 * @throws VariantRuntimeException 
	 */
	public static VariantViewRequest startViewRequest(VariantSession session, View view) {
		
		stateCheck();
		// It's caller's responsibility to init the targeting persister.
		if (session.getTargetingPersister() == null) {
			throw new VariantRuntimeException(ErrorTemplate.RUN_TP_NOT_INITIALIZED);
		}
		return VariantRuntime.startViewRequest(session, view);
	}
	
	/**
	 * End of a view request.
	 * @param request
	 */
	public static void commitViewRequest(VariantViewRequest request) {

		stateCheck();
		if (((VariantViewRequestImpl)request).isCommitted()) {
			throw new IllegalStateException("Request already committed");
		}
		VariantRuntime.commitViewRequest(request);
	}
	
	/**
	 * Variant logger.
	 * @return
	 */
	public static Logger getLogger() {
		return logger;
	}
	
	/**
	 * 
	 * @return
	 */
	public static EventWriter getEventWriter() {
		stateCheck();
		return eventWriter;
	}
	
}
