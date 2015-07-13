package com.variant.core;

import java.io.InputStreamReader;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.conf.RuntimeService;
import com.variant.core.error.ErrorTemplate;
import com.variant.core.error.Severity;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.runtime.VariantRuntime;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;
import com.variant.core.schema.impl.ParserResponse;
import com.variant.core.schema.impl.SchemaParser;
import com.variant.core.session.SessionKeyResolver;
import com.variant.core.session.SessionService;
import com.variant.core.session.TargetingPersister;

/**
 * The Variant Container.
 * 
 * @author Igor
 *
 */
public class Variant {

	private static Logger logger = LoggerFactory.getLogger("Variant");
	private static Config config = null;
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
		return version == null ? "?" : "V. " + version + " (Alpha), Copyright (C) 2015 getvariant.com";
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Container bootstrap from an InputStream.
	 * 
	 * @param config
	 */
	public static void bootstrap(InputStreamReader config) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Variant container bootstrap from a data structure.\
	 * This must be the first call to Variant, before any other methods may be called.
	 * Calling this 2nd time over the life of the JVM will throw an IllegalStateException.
	 * 
	 * @param config
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void bootstrap(Config config) throws VariantBootstrapException {
		
		if (isBootstrapped) throw new IllegalStateException("Variant is already initialized");

		long now = System.currentTimeMillis();
		
		//
		// Instantiate event persister.
		//
		if (config.eventPersisterClassName == null) {
			throw new IllegalArgumentException("Property [eventPersisterClassName] must be set");
		}
		
		EventPersister eventPersister = null;
		try {
			Object eventPersisterObject = Class.forName(config.eventPersisterClassName).newInstance();
			if (eventPersisterObject instanceof EventPersister) {
				eventPersister = (EventPersister) eventPersisterObject;
			}
			else {
				throw new VariantBootstrapException(
						"Event persister class [" + 
				config.eventPersisterClassName + 
				"] must implement interface [" +
				EventPersister.class.getName()
				);
			}
		}
		catch (Exception e) {
			throw new VariantBootstrapException(
					"Unable to instantiate event persister class [" + config.eventPersisterClassName +"]",
					e
			);
		}
		
		// Instantiate event writer.
		eventWriter = new EventWriter(config.eventWriterConfig, eventPersister);
		
		// Pass the config to the new object.
		eventPersister.initialized(config.eventPersisterConfig);
		
		//
		// Instantiate session service.
		//
		sessionService = new SessionService(config.sessionServiceConfig);

		Variant.config = config;
		
		isBootstrapped = true;
		
		logger.info(
				String.format("Core [%s] bootstrapped in %s",
						version(),
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
	}
	
	/**
	 * Container configuration.
	 * @return
	 */
	public static Config getConfig() {
		return config;
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
		ParserResponse result = SchemaParser.parse(schemaAsString);
		// Only replace the schema if no ERROR or higher level errors.
		if (result.highestSeverity().lessThan(Severity.ERROR)) {
			schema = result.getSchema();
			StringBuilder msg = new StringBuilder("New schema deployed in ");
			msg.append(DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")).append(":");
			for (Test test: schema.getTests()) {
				msg.append("\n   ").append(test.getName()).append("(");
				boolean first = true;
				for (Experience exp: test.getExperiences()) {
					if (first) first = false;
					else msg.append(", ");
					msg.append(exp.getName());
					if (exp.isControl()) msg.append(" (control)");
				}
				msg.append(")");
			}
			logger.info(msg.toString());
		}
		else {
			logger.error("New schema was not deployed due to parser error(s).");
		}
		
		return result;
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
	 */
	public static VariantSession getSession(SessionKeyResolver.UserData userData) {
		return sessionService.getSession(true, userData);
	}

	/**
     * Start view Request 
	 * @return
	 */
	public static void startViewRequest(VariantSession session, View view) {
		
		stateCheck();
		
		// It's caller's responsibility to init the targeting persister.
		if (session.getTargetingPersister() == null) {
			throw new VariantRuntimeException(ErrorTemplate.RUN_TP_NOT_INITIALIZED);
		}
		
		VariantRuntime.targetSession(session, view);		
	}
	
	/**
	 * End of a view request.
	 * @param request
	 */
	public static void endViewRequest(VariantViewRequest request) {
		stateCheck();		
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
	
 	/**
	 * For programmatic configuration.
	 * @author Igor
	 *
	 */
	public static class Config {
		
		// Default is in-memory H2.
		private String eventPersisterClassName = "com.variant.ext.persist.EventPersisterH2";
		private EventPersister.Config eventPersisterConfig = new EventPersister.Config();
		private TargetingPersister.Config targetingPersisterConfig = new TargetingPersister.Config();
		private EventWriter.Config eventWriterConfig = new EventWriter.Config();
		private SessionService.Config sessionServiceConfig = new SessionService.Config();
		
		/**
		 * Default values.
		 */
		public Config() {}
		
		/**
		 * 
		 * @param eventPersisterClassName
		 */
		public void setEventPersisterClassName(String eventPersisterClassName) {
			this.eventPersisterClassName = eventPersisterClassName;
		}

		/**
		 * 
		 * @return
		 */
		public String getEventPersisterClassName() {
			return eventPersisterClassName;
		}
		
		/**
		 * 
		 * @return
		 */
		public void setEventPersisterConfig(EventPersister.Config config) {
			this.eventPersisterConfig = config;
		}
		
		/**
		 * 
		 * @return
		 */
		public EventPersister.Config getEventPersisterConfig() {
			return eventPersisterConfig;
		}
		
		/**
		 * 
		 * @param config
		 */
		public void setEventWriterConfig(EventWriter.Config config) {
			this.eventWriterConfig = config;
		}

		/**
		 * 
		 * @return
		 */
		public EventWriter.Config getEventWriterConfig() {
			return eventWriterConfig;
		}
		
		/**
		 * 
		 * @param config
		 */
		public void setSessioinServiceConfig(SessionService.Config config) {
			this.sessionServiceConfig = config;
		}

		/**
		 * 
		 * @return
		 */
		public SessionService.Config getSessionServiceConfig() {
			return sessionServiceConfig;
		}
		
		/**
		 * 
		 * @return
		 */
		public TargetingPersister.Config getTargetingPersisterConfig() {
			return targetingPersisterConfig;
		}

		/**
		 * 
		 * @param targetingPersisterConfig
		 */
		public void setTargetingPersisterConfig(
				TargetingPersister.Config targetingPersisterConfig) {
			this.targetingPersisterConfig = targetingPersisterConfig;
		}

	}
}
