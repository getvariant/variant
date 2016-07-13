package com.variant.core.impl;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.VariantCorePropertyKeys.Key;
import com.variant.core.VariantProperties;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.PropertiesChain;
import com.variant.core.util.Tuples.Pair;

/**
 * Static singleton interface to system properties.
 * Initially created by Core with default values only. Downstream users will add their
 * own properties via <code>overrideFromResource()</code>
 * 
 * @author Igor
 *
 */
public class CorePropertiesImpl implements VariantProperties {
	
	public static final String COMMANDLINE_RESOURCE_NAME = "variant.props.resource";
	public static final String COMMANDLINE_FILE_NAME = "varaint.props.file";
	public static final String COMMANDLINE_PROP_PREFIX = "varaint.";

	private PropertiesChain propsChain = new PropertiesChain();
	private VariantCore coreApi;

	/**
	 * 
	 */
	protected CorePropertiesImpl(VariantCore coreApi) {
		this.coreApi = coreApi;
		// if we're called by a subclass, coreApi will already have a properties chain.
		propsChain = new PropertiesChain();
	}

	/**
	 * Override with properties sourced from an InputStream.
	 * @param resourceName
	 */
	void overrideWith(InputStream is, String comment) {
					
		try {
			Properties properties = new Properties();
			properties.load(is);
			propsChain.overrideWith(properties, comment);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to read input stream", t);
		}
	}

	/**
	 * Integer value
	 * @param key
	 * @return
	 */
	private Integer getInteger(Key key) {
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
	Pair<String, String> getString(Key key) {
		
		Pair<String,String> result = null;
		
		// Try override via JVM prop
		String value = System.getProperty(COMMANDLINE_PROP_PREFIX + key.propertyName());
		
		if (value == null) {
			// No JVM override
			result = propsChain.getProperty(key.propertyName());
		}
		else {
			// JVM override takes precedence.
			result = new Pair<String, String>(value, "JVM Property");
		}
		
		return result == null ? new Pair<String, String>(key.defaultValue(), "Default") : result;
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
	Map<String, String> getMap(Key key) {
		String raw = getString(key).arg1();
		try {
			ObjectMapper jacksonDataMapper = new ObjectMapper();
			jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			return jacksonDataMapper.readValue(raw, Map.class);
		}
		catch (Exception e) {
			throw new VariantRuntimeException(RUN_PROPERTY_INIT_INVALID_JSON, raw, key.propertyName());
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Key key, Class<T> clazz) {
		if (clazz == String.class)
			return (T) getString(key).arg1();
		else if (clazz == Integer.class)
			return (T) getInteger(key);
		else if (clazz == Map.class)
			return (T) getMap(key);
		else 
			throw new VariantRuntimeException(MessageTemplate.RUN_PROPERTY_BAD_CLASS, clazz.getName());
	}
	
	/**
	 * <p> Equivalent to <code>get(key, String.class)</code>.
	 * 
	 * @param key Property key
	 *              
	 * @return Raw String value.
	 */	
	public String get(Key key) {
		return getString(key).arg1();
	}

	/**
	 * The source of where the value came from, such as the name of the application properties resurce file.
	 *  
	 * @param key
	 * @return
	 */
	public String getSource(Key key) {
		return getString(key).arg2();
	}

}
