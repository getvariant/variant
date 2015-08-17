package com.variant.core;

import java.io.InputStream;
import java.util.Properties;

import com.variant.core.session.SessionStore;
import com.variant.core.util.PropertiesChain;
import com.variant.core.util.VariantIoUtils;

/**
 * Static singleton interface to system properties.
 * Initially created by Core with default values only. Downstream users will add their
 * own properties via <code>overrideFromResource()</code>
 * 
 * @author Igor
 *
 */
public class VariantProperties {

	private static VariantProperties instance = null;	
	
	private PropertiesChain props = new PropertiesChain();
	
	/**
	 * 
	 */
	private VariantProperties() {
		props = new PropertiesChain();
		override(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));		
	}

	

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
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
			return getInstance().props.getString(propName());
		}
	}

	/**
	 * Factory
	 * @return
	 */
	public static VariantProperties getInstance() {
		if (instance == null) {
			instance = new VariantProperties();
		}
		return instance;
	}

	/**
	 * Override with properties sourced from an InputStream.
	 * @param resourceName
	 */
	public void override(InputStream is) {
					
		try {
			Properties properties = new Properties();
			properties.load(is);
			props.add(properties);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to read input stream", t);
		}
	}

	public String eventPersisterClassName() {
		return props.getString(Keys.EVENT_PERSISTER_CLASS_NAME.propName());
	}

	public String eventPersisterJdbcUrl() {
		return props.getString(Keys.EVENT_PERSISTER_JDBC_URL.propName());
	}

	public String eventPersisterJdbcUser() {
		return props.getString(Keys.EVENT_PERSISTER_JDBC_USER.propName());
	}

	public String eventPersisterJdbcPassword() {
		return props.getString(Keys.EVENT_PERSISTER_JDBC_PASSWORD.propName());
	}

	public String targetingPersisterClassName() {
		return props.getString(Keys.TARGETING_PERSISTER_CLASS_NAME.propName());
	}
	
	public int targetingPersisterIdleDaysToLive() {
		return props.getInteger(Keys.TARGETING_PERSISTER_IDLE_DAYS_TO_LIVE.propName());
	}

	public SessionStore.Type sessionStoreType() {
		String val = props.getString(Keys.SESSION_STORE_TYPE.propName());
		return SessionStore.Type.valueOf(val.toUpperCase());
	}
	
	public String sessionKeyResolverClassName() {
		return props.getString(Keys.SESSION_KEY_RESOLVER_CLASS_NAME.propName());
	}

	public int eventWriterBufferSize() {
		return props.getInteger(Keys.EVENT_WRITER_BUFFER_SIZE.propName());	
	}

	public Integer eventWriterPercentFull() {
		return props.getInteger(Keys.EVENT_WRITER_PERCENT_FULL.propName());	
	}

	public Integer eventWriterMaxDelayMillis() {
		return props.getInteger(Keys.EVENT_WRITER_MAX_DELAY_MILLIS.propName());			
	}
}
