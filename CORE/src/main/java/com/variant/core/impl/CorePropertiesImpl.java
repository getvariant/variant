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
import com.variant.core.VariantCoreProperties;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.PropertiesChain;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.util.VariantIoUtils;

/**
 * Static singleton interface to system properties.
 * Initially created by Core with default values only. Downstream users will add their
 * own properties via <code>overrideFromResource()</code>
 * 
 * @author Igor
 *
 */
public class CorePropertiesImpl implements VariantCoreProperties{
	
	private PropertiesChain propsChain = new PropertiesChain();
	private VariantCore coreApi;

	/**
	 * 
	 */
	protected CorePropertiesImpl(VariantCore coreApi) {
		this.coreApi = coreApi;
		propsChain = new PropertiesChain();
		// Publicly visible defaults.props is actually the second to last on the chain.
		overrideWith(VariantIoUtils.openResourceAsStream("/variant/defaults.props"), "/variant/defaults.props");
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

	/**
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T get(Key key, Class<T> clazz) {
		if (clazz == String.class)
			return (T) getString(key.propertyName()).arg1();
		else if (clazz == Integer.class)
			return (T) getInteger(key.propertyName());
		else if (clazz == Map.class)
			return (T) getMap(key.propertyName());
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
		return getString(key.propertyName()).arg1();
	}

	/**
	 * The source of where the value came from, such as the name of the application properties resurce file.
	 *  
	 * @param key
	 * @return
	 */
	public String getSource(Key key) {
		return getString(key.propertyName()).arg2();
	}

}
