package com.variant.core.conf;

import com.variant.core.util.PropertiesChain;

/**
 * Static singleton interface to system properties.
 * Initially created by Core with default values only. Downstream users will add their
 * own properties via <code>overrideFromResource()</code>
 * 
 * @author Igor
 *
 */
public class VariantProperties {

	private static PropertiesChain props = new PropertiesChain();
	static {
		props.addFromResource("/variant-defaults.props");
	}
	
	/**
	 * Static singleton
	 */
	private VariantProperties() {}

	/**
	 * 
	 */
	public static enum Keys {
		
		EVENT_PERSISTER_CLASS_NAME,
		EVENT_PERSISTER_JDBC_PASSWORD,
		EVENT_PERSISTER_JDBC_URL,
		EVENT_PERSISTER_JDBC_USER,
		EVENT_WRITER_BUFFER_SIZE,
		EVENT_WRITER_MAX_DELAY_MILLIS,
		EVENT_WRITER_PERCENT_FULL,
		SESSION_KEY_RESOLVER_CLASS_NAME,
		TARGETING_PERSISTER_IDLE_DAYS_TO_LIVE,
		;
		
		/**
		 * 
		 * @return
		 */
		public String propName() {
			StringBuilder result = new StringBuilder(this.name().toLowerCase());
			for (int i = 0; i < result.length(); i++) {
				if (result.charAt(i) == '_') {
					result.setCharAt(i, '.');
				}
			}
			return result.toString();
		}
		
		/**
		 * 
		 * @return
		 */
		public String propValue() {
			return props.getString(propName());
		}
	}
	/**
	 * Override with properties object sourced from a classpath resource.
	 * @param resourceName
	 */
	public static void overrideFromResource(String resourceName) {
		VariantProperties.props.addFromResource(resourceName);
	}
	
	public static String eventPersisterClassName() {
		return props.getString(Keys.EVENT_PERSISTER_CLASS_NAME.propName());
	}

	public static String eventPersisterJdbcUrl() {
		return props.getString(Keys.EVENT_PERSISTER_JDBC_URL.propName());
	}

	public static String eventPersisterJdbcUser() {
		return props.getString(Keys.EVENT_PERSISTER_JDBC_USER.propName());
	}

	public static String eventPersisterJdbcPassword() {
		return props.getString(Keys.EVENT_PERSISTER_JDBC_PASSWORD.propName());
	}

	public static int targetingPersisterIdleDaysToLive() {
		return props.getInteger(Keys.TARGETING_PERSISTER_IDLE_DAYS_TO_LIVE.propName());
	}
	
	public static String getSessionKeyResolverClassName() {
		return props.getString(Keys.SESSION_KEY_RESOLVER_CLASS_NAME.propName());
	}
	
	public static int getEventWriterBufferSize() {
		return props.getInteger(Keys.EVENT_WRITER_BUFFER_SIZE.propName());	
	}

	public static Integer getEventWriterPercentFull() {
		return props.getInteger(Keys.EVENT_WRITER_PERCENT_FULL.propName());	
	}

	public static Integer getEventWriterMaxDelayMillis() {
		return props.getInteger(Keys.EVENT_WRITER_MAX_DELAY_MILLIS.propName());			
	}
}
