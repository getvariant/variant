package com.variant.core;

import java.io.InputStream;
import java.util.Properties;

import com.variant.core.session.SessionStore;
import com.variant.core.util.PropertiesChain;
import com.variant.core.util.VariantIoUtils;
import com.variant.core.util.VariantStringUtils;

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
		override(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));
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
		SESSION_STORE_TYPE,
		SESSION_KEY_RESOLVER_CLASS_NAME,
		TARGETING_PERSISTER_CLASS_NAME,
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

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * Override with properties sourced from an InputStream.
	 * @param resourceName
	 */
	public static void override(InputStream is) {
					
		try {
			Properties properties = new Properties();
			properties.load(is);
			props.add(properties);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to read input stream", t);
		}
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

	public static String targetingPersisterClassName() {
		return props.getString(Keys.TARGETING_PERSISTER_CLASS_NAME.propName());
	}
	
	public static int targetingPersisterIdleDaysToLive() {
		return props.getInteger(Keys.TARGETING_PERSISTER_IDLE_DAYS_TO_LIVE.propName());
	}

	public static SessionStore.Type sessionStoreType() {
		String val = props.getString(Keys.SESSION_STORE_TYPE.propName());
		return SessionStore.Type.valueOf(val.toUpperCase());
	}
	
	public static String sessionKeyResolverClassName() {
		return props.getString(Keys.SESSION_KEY_RESOLVER_CLASS_NAME.propName());
	}

	public static int eventWriterBufferSize() {
		return props.getInteger(Keys.EVENT_WRITER_BUFFER_SIZE.propName());	
	}

	public static Integer eventWriterPercentFull() {
		return props.getInteger(Keys.EVENT_WRITER_PERCENT_FULL.propName());	
	}

	public static Integer eventWriterMaxDelayMillis() {
		return props.getInteger(Keys.EVENT_WRITER_MAX_DELAY_MILLIS.propName());			
	}
}
