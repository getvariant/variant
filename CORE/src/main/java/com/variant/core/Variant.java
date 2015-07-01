package com.variant.core;

import java.io.InputStreamReader;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.config.TestConfig;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;
import com.variant.core.runtime.VariantRuntime;
import com.variant.core.session.SessionService;

/**
 * The Variant Container.
 * 
 * @author Igor
 *
 */
public class Variant {

	private static Logger logger = LoggerFactory.getLogger("Variant");
	private static boolean isBootstrapped = false;
	private static TestConfig testConfig = null;
	private static EventWriter eventWriter = null;
	private static SessionService sessionService = null;
	
	// Tests will call this to replace logger implementation.
	static void setLogger(Logger logger) {
		Variant.logger = logger;
	}

	/**
	 * Although there's no way not to make this public, client code should not call this. 
	 */
	public static  void setTestConfig(TestConfig config) {
		Variant.testConfig = config;
	}
	
	/**
	 * Static singleton.
	 */
	private Variant() {}
	
	
	private static void stateCheck() {
		if (!isBootstrapped) throw new IllegalStateException("Variant must be initialized first");
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
	 * Bootstrap from a data structure.
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
		if (config.persisterClassName == null) {
			throw new IllegalArgumentException("Property [persistorClassName] must be set");
		}
		
		EventPersister persister = null;
		try {
			Class<?> persisterClass = Class.forName(config.persisterClassName);
			Object persisterObject = persisterClass.newInstance();
			if (persisterObject instanceof EventPersister) {
				persister = (EventPersister) persisterObject;
			}
			else {
				throw new VariantBootstrapException(
						"Event bootstrapper class [" + 
				config.persisterClassName + 
				"] must implement interface [" +
				EventPersister.class.getName()
				);
			}
		}
		catch (Exception e) {
			throw new VariantBootstrapException(
					"Unable to instantiate event bootstrapper class [" +
					config.persisterClassName +
					"]",
					e
			);
		}
		
		// Session Service.
		sessionService = new SessionService(config.sessionServiceConfig);

		// Instantiate event writer.
		eventWriter = new EventWriter(config.eventWriterConfig, persister);

		// User callback
		persister.initialized(config.persisterConfig);
				
		isBootstrapped = true;
		
		logger.info("Variant bootstrapped in " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - now));
	}
	
	/**
	 * Get current configuration.
	 * @return
	 */
	public static TestConfig getTestConfig() {

		stateCheck();
		return testConfig;
	
	}
	
	/**
	 * 
	 * @param create
	 * @return
	 */
	public static VariantSession getSession(boolean create) {
		return sessionService.getSession(true);
	}
	
	/**
	 * Start of a view request.
	 * 1. Look up the view by its path.  If the view is not known, return null.  
	 * 2. Find all the tests instrumented on this view.  This is the view's test list,
	 *    i.e. the list of tests to be targeted before we can resolve the view.
	 * 3. Target all these tests, i.e. figure out an experience for each of them. 
	 * 4. Find the subset of the view's test list that are already targeted by consulting the 
	 *    targeting persister.
     * 5. If such a subset exists, confirm that we are able to handle this test cell. 
     *    The reason we may not is if the two tests used to be covariant and the experience
     *    persister reports two already targeted variant experiences, but in a recent config 
     *    change these two tests are no longer covariant and hence we don't know how to resolve
     *    this test cell.
     * 6. If we're still able to resolve the pre-targeted cell, continue with the rest of the tests 
     *    on the view's list in the order they were defined, and target each test via the regular 
     *    targeting mechanism.
     * 7. If we're unable to resolve the pre-targeted cell, remove all its experience from the
     *    tergeting persister, i.e. make it equivalent to there being no pre-targed tests at all
     *    and target all tests, in the order they were defined, regularly.
     *
	 * @return
	 */
	public static void startViewRequest(VariantSession session, String viewPath) {
		
		stateCheck();
		VariantRuntime.targetSession(session, viewPath);		
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
		stateCheck();
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
		private String persisterClassName = "com.variant.ext.persist.EventPersisterH2";
		private EventPersister.Config persisterConfig = new EventPersister.Config();
		private EventWriter.Config eventWriterConfig = new EventWriter.Config();
		private SessionService.Config sessionServiceConfig = new SessionService.Config();
		
		/**
		 * Default values.
		 */
		public Config() {}
		
		/**
		 * 
		 * @param persisterClassName
		 */
		public void setPersisterClassName(String persisterClassName) {
			this.persisterClassName = persisterClassName;
		}

		/**
		 * 
		 * @return
		 */
		public String getPersisterClassName() {
			return persisterClassName;
		}
		
		/**
		 * 
		 * @return
		 */
		public void setPersisterConfig(EventPersister.Config config) {
			this.persisterConfig = config;
		}
		
		/**
		 * 
		 * @return
		 */
		public EventPersister.Config getEventPersisterConfig() {
			return persisterConfig;
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
	}
}
