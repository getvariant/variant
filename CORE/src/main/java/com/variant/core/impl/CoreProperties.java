package com.variant.core.impl;

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
import com.variant.core.InitializationParams;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.PropertiesChain;
import com.variant.core.util.Tuples.Pair;
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
public class CoreProperties {
	
	private PropertiesChain propsChain = new PropertiesChain();
	private VariantCore coreApi;

	/**
	 * 
	 */
	CoreProperties(VariantCore coreApi) {
		this.coreApi = coreApi;
		propsChain = new PropertiesChain();
		// Internal is the last on the chain
		overrideWith(VariantIoUtils.openResourceAsStream("/variant/internal." + VariantStringUtils.RESOURCE_POSTFIX + ".props"), "INTERNAL");
		// Publicly visible defaults.props is actually the second to last on the chain.
		overrideWith(VariantIoUtils.openResourceAsStream("/variant/defaults.props"), "/variant/defaults.props");
	}

	/**
	 * Integer value
	 * @param key
	 * @return
	 */
	private Integer getInteger(String key) {
		return Integer.parseInt(getString(key).arg1());
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
	 * Open direct access to Property chain for tests.
	 * System property overrides.
	 * @param name
	 * @return
	 */
	Pair<String, String> getString(String key) {		
		String value = System.getProperty(COMMANDLINE_PROP_PREFIX + key);
		if (value == null) {
			Pair<String, String> result = propsChain.getProperty(key);
			if (result == null) throw new VariantRuntimeException(RUN_PROPERTY_NOT_SET, key);
			else return result;
		}
		else {
			return new Pair<String, String>(value, "JVM Property");
		}
	}

	/**
	 * Open direct access to Property chain for tests.
	 * System property overrides.
	 * @param name
	 * @return
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 */
	@SuppressWarnings("unchecked")
	Map<String, String> getMap(String key) {
		String raw = getString(key).arg1();
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

	public static final String COMMANDLINE_RESOURCE_NAME = "variant.props.resource";
	public static final String COMMANDLINE_FILE_NAME = "varaint.props.file";
	public static final String COMMANDLINE_PROP_PREFIX = "varaint.";

	/**
	 * <p>Keys of all known properties.
	 */
	public static enum Key {
		
		EVENT_PERSISTER_CLASS_NAME,
		EVENT_PERSISTER_CLASS_INIT,
		EVENT_WRITER_BUFFER_SIZE,
		EVENT_WRITER_MAX_DELAY_MILLIS,
		EVENT_WRITER_PERCENT_FULL,
		SESSION_STORE_CLASS_NAME,
		SESSION_STORE_CLASS_INIT,
		SESSION_ID_TRACKER_CLASS_NAME,
		SESSION_ID_TRACKER_CLASS_INIT,
		TARGETING_TRACKER_CLASS_NAME,
		TARGETING_TRACKER_IDLE_DAYS_TO_LIVE,
		
		SERVER_ENDPOINT_URL
		;		
		
		/**
		 * 
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
	}

	/**
	 * Override with properties sourced from an InputStream.
	 * @param resourceName
	 */
	public void overrideWith(InputStream is, String comment) {
					
		try {
			Properties properties = new Properties();
			properties.load(is);
			propsChain.overrideWith(properties, comment);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to read input stream", t);
		}
	}
	
	/**
	 * <p> Interpreted value of a property parameterized by expected type.
	 * 
	 * @param key Property key
	 * @param clazz Class of the expected return object.  <code>String.class</class> can be always used,
	 *              in which case the raw value of the property is returned. If <code>Integer.class</code>
	 *              is used, the raw string value will be converted to integer. If {@link InitializationParams}.class
	 *              is used, the raw string will be parsed as JSON.
	 *              
	 * @return Raw or interpreted value.
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(Key key, Class<T> clazz) {
		if (clazz == String.class)
			return (T) getString(key.propName()).arg1();
		else if (clazz == Integer.class)
			return (T) getInteger(key.propName());
		else if (clazz == InitializationParams.class)
			return (T) new InitializationParamsImpl(coreApi, getMap(key.propName()));
		else 
			throw new VariantRuntimeException(MessageTemplate.RUN_PROPERTY_BAD_CLASS, clazz.getName());
	}
	
	/**
	 * <p> Equivalent to <code>get(key, Class<String>)</code>.
	 * 
	 * @param key Property key
	 *              
	 * @return Raw String value.
	 */	
	public String get(Key key) {
		return getString(key.propName()).arg1();
	}

	/**
	 * The source of where the value came from, such as the name of the application properties resurce file.
	 *  
	 * @param key
	 * @return
	 */
	public String getSource(Key key) {
		return getString(key.propName()).arg2();
	}

}
