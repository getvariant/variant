package com.variant.core.config;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON;
import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_NOT_SET;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.VariantRuntimeException;
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

	public static final String RUNTIME_PROPS_RESOURCE_NAME = "varaint.props.resource";
	public static final String RUNTIME_PROPS_FILE_NAME = "varaint.props.file";

	private static VariantProperties instance = null;	
	
	private PropertiesChain props = new PropertiesChain();

	/**
	 * 
	 */
	VariantProperties() {
		props = new PropertiesChain();
		override(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));		
	}

	/**
	 * Integer value
	 * @param key
	 * @return
	 */
	private int getInteger(String key) {
		return Integer.parseInt(getString(key));
	}

	/**
	 * Boolean value
	 * @param key
	 * @return
	 *
	private boolean getBoolean(String key) {
		return Boolean.parseBoolean(getString(key));
	}*/
	
	/**
	 * Open direct access to Property chain for junits.
	 * System property overrides.
	 * @param name
	 * @return
	 */
	String getString(String key) {		
		String result = System.getProperty(key);
		if (result == null) result = props.getProperty(key);
		if (result == null) throw new VariantRuntimeException(RUN_PROPERTY_NOT_SET, key);
		return result;
	}

	/**
	 * Open direct access to Property chain for junits.
	 * System property overrides.
	 * @param name
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@SuppressWarnings("unchecked")
	Map<String, String> getMap(String key) {
		String raw = getString(key);
		try {
			ObjectMapper jacksonDataMapper = new ObjectMapper();
			jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			return jacksonDataMapper.readValue(raw, Map.class);
		}
		catch (Exception e) {
			throw new VariantRuntimeException(RUN_PROPERTY_INIT_INVALID_JSON, raw, key);
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * 
	 */
	public static enum Keys {
		
		EVENT_PERSISTER_CLASS_NAME,
		EVENT_PERSISTER_CLASS_INIT,
		EVENT_WRITER_BUFFER_SIZE,
		EVENT_WRITER_MAX_DELAY_MILLIS,
		EVENT_WRITER_PERCENT_FULL,
		SESSION_STORE_CLASS_NAME,
		TARGETING_TRACKER_CLASS_NAME,
		TARGETING_TRACKER_IDLE_DAYS_TO_LIVE,
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
			return getInstance().getString(propName());
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
		return getString(Keys.EVENT_PERSISTER_CLASS_NAME.propName());
	}

	public String targetingTrackerClassName() {
		return getString(Keys.TARGETING_TRACKER_CLASS_NAME.propName());
	}
	
	public int targetingTrackerIdleDaysToLive() {
		return getInteger(Keys.TARGETING_TRACKER_IDLE_DAYS_TO_LIVE.propName());
	}

	public String sessionStoreClassName() {
		return getString(Keys.SESSION_STORE_CLASS_NAME.propName());
	}
	
	public Map<String, String> eventPersisterClassInit() {
		return getMap(Keys.EVENT_PERSISTER_CLASS_INIT.propName());
	}

	public int eventWriterBufferSize() {
		return getInteger(Keys.EVENT_WRITER_BUFFER_SIZE.propName());	
	}

	public int eventWriterPercentFull() {
		return getInteger(Keys.EVENT_WRITER_PERCENT_FULL.propName());	
	}

	public int eventWriterMaxDelayMillis() {
		return getInteger(Keys.EVENT_WRITER_MAX_DELAY_MILLIS.propName());			
	}
}
