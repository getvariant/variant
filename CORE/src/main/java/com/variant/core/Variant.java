package com.variant.core;

import java.io.InputStreamReader;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.config.TestConfig;
import com.variant.core.event.EventPersister;
import com.variant.core.event.EventWriter;

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
		
		// User callback
		persister.initialized(config.persisterConfig);
		
		//
		// Instantiate event writer.
		//
		eventWriter = new EventWriter(config.eventWriterConfig, persister);
		
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
	 * Start of a view request.
	 * @return
	 */
	public static VariantViewRequest startViewRequest() {
		stateCheck();
		return null;
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
		
		private String persisterClassName = null;
		private EventPersister.Config persisterConfig;
		private EventWriter.Config eventWriterConfig;
		
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
		public void setPersisterConfig(EventPersister.Config config) {
			this.persisterConfig = config;
		}
		
		/**
		 * 
		 * @param config
		 */
		public void setEventWriterConfig(EventWriter.Config config) {
			this.eventWriterConfig = config;
		}
	}
}
