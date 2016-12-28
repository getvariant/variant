package com.variant.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.Properties;
import com.variant.core.event.impl.util.PropertiesChain;
import com.variant.core.util.Tuples.Pair;

/**
 * Static singleton interface to system properties.
 * Initially created by Core with default values only. Downstream users will add their
 * own properties via <code>overrideFromResource()</code>
 * 
 * @author Igor
 *
 */
public class SystemPropertiesImpl implements Properties {
	
	public static final String COMMANDLINE_RESOURCE_NAME = "variant.props.resource";
	public static final String COMMANDLINE_FILE_NAME = "varaint.props.file";
	public static final String COMMANDLINE_PROP_PREFIX = "varaint.";

	private PropertiesChain propsChain = new PropertiesChain();

	/**
	 * 
	 */
	protected SystemPropertiesImpl() {
		// if we're called by a subclass, coreApi will already have a properties chain.
		propsChain = new PropertiesChain();
	}

	/**
	 * Override with properties sourced from an InputStream.
	 * @param resourceName
	 */
	void overrideWith(InputStream is, String comment) {
					
		try {
			java.util.Properties props = new java.util.Properties();
			props.load(is);
			propsChain.overrideWith(props, comment);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to read input stream", t);
		}
	}

	/**
	 * Integer value
	 * @param key
	 * @return
	 */
	private Integer getInteger(Property key) {
		return Integer.parseInt(getString(key)._1());
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
	Pair<String, String> getString(Property key) {
		
		Pair<String,String> result = null;
		
		// Try override via JVM prop
		String value = System.getProperty(COMMANDLINE_PROP_PREFIX + key.name);
		
		if (value == null) {
			// No JVM override
			result = propsChain.getProperty(key.name());
		}
		else {
			// JVM override takes precedence.
			result = new Pair<String, String>(value, "JVM Property");
		}
		
		return result == null ? new Pair<String, String>(key.defaultValue, "Default") : result;
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
	Map<String, String> getMap(Property key) {
		String raw = getString(key)._1();
		try {
			ObjectMapper jacksonDataMapper = new ObjectMapper();
			jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			return jacksonDataMapper.readValue(raw, Map.class);
		}
		catch (Exception e) {
			throw new ClientErrorException(ClientError.PROPERTY_INIT_INVALID_JSON, raw, key.name);
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
	public <T> T get(Property key, Class<T> clazz) {
		if (clazz == String.class)
			return (T) getString(key)._1();
		else if (clazz == Integer.class)
			return (T) getInteger(key);
		else if (clazz == Map.class)
			return (T) getMap(key);
		else 
			throw new ClientErrorException(ClientError.PROPERTY_BAD_CLASS, clazz.getName());
	}
	
	/**
	 * <p> Equivalent to <code>get(key, String.class)</code>.
	 * 
	 * @param key Property key
	 *              
	 * @return Raw String value.
	 */	
	@Override
	public String get(Property key) {
		return getString(key)._1();
	}

	/**
	 * The source of where the value came from, such as the name of the application properties resurce file.
	 *  
	 * @param key
	 * @return
	 */
	@Override
	public String getSource(Property key) {
		return getString(key)._2();
	}

}