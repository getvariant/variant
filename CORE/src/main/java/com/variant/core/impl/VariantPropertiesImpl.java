package com.variant.core.impl;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_INVALID_JSON;
import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_NOT_SET;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.InitializationParams;
import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.config.PropertiesChain;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.util.VariantIoUtils;

/**
 * Static singleton interface to system properties.
 * Initially created by Core with default values only. Downstream users will add their
 * own properties via <code>overrideFromResource()</code>
 * 
 * @author Igor
 *
 */
public class VariantPropertiesImpl implements VariantProperties {

	public static final String RUNTIME_PROPS_RESOURCE_NAME = "varaint.props.resource";
	public static final String RUNTIME_PROPS_FILE_NAME = "varaint.props.file";
	
	private PropertiesChain propsChain = new PropertiesChain();
	private VariantCoreImpl coreApi;

	/**
	 * 
	 */
	VariantPropertiesImpl(VariantCoreImpl coreApi) {
		this.coreApi = coreApi;
		propsChain = new PropertiesChain();
		override(VariantIoUtils.openResourceAsStream("/variant-defaults.props"));		
	}

	/**
	 * Integer value
	 * @param key
	 * @return
	 */
	private Integer getInteger(String key) {
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
		if (result == null) result = propsChain.getProperty(key);
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
	 * Override with properties sourced from an InputStream.
	 * @param resourceName
	 */
	public void override(InputStream is) {
					
		try {
			Properties properties = new Properties();
			properties.load(is);
			propsChain.add(properties);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to read input stream", t);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Key key, Class<T> clazz) {
		if (clazz == String.class)
			return (T) getString(key.propName());
		else if (clazz == Integer.class)
			return (T) getInteger(key.propName());
		else if (clazz == InitializationParams.class)
			return (T) new InitParams(coreApi, getMap(key.propName()));
		else 
			throw new VariantRuntimeException(MessageTemplate.RUN_PROPERTY_BAD_CLASS, clazz.getName());
	}
	
	/**
	 * 
	 */
	@SuppressWarnings("serial")
	public static class InitParams extends HashMap<String, String> implements InitializationParams {
		
		private Variant coreApi; 
		
		private InitParams(Variant coreApi, Map<String,String> map) {
			super(map);
			this.coreApi = coreApi;
		}
		
		@Override
		public String get(String param) {
			return super.get(param);
		}

		@Override
		public String getOrThrow(String key, VariantRuntimeException e) {
			String result = super.get(key);
			if (result == null) throw e;
			else return result;
		}

		@Override
		public Variant getCoreApi() {
			return coreApi;
		}

	}

}
