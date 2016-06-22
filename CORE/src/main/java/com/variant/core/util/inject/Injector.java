package com.variant.core.util.inject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.VariantCore;
import com.variant.core.util.VariantIoUtils;

/**
 * Implementation injection, deferred to run time.
 * Lazily instantiated by first invocation.  Always look for injector.json on classpath.
 * 
 * @author Igor
 * @since 0.6
 */
public class Injector {

	private static final String INJECTOR_CONFIG_RESOURCE_NAME = "/variant/injector.json";
	private static HashMap<Class<? extends Injectable>, Object> implMap = null;
		
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private static void lazyInit(VariantCore core) {
		
		InputStream configStream = VariantIoUtils.openResourceAsStream(INJECTOR_CONFIG_RESOURCE_NAME);
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		List<Map<String, ?>> parseTree = null;

		// Parse the entire config in memory.
		try {
			parseTree = jacksonDataMapper.readValue(configStream, List.class);
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to parse injector config [" + INJECTOR_CONFIG_RESOURCE_NAME + "]", e);
		}
		
		HashMap<Class<? extends Injectable>, Object> result = new HashMap<Class<? extends Injectable>, Object>();
		
		// for each config entry
		for (Map<String, ?> entry: parseTree) {
			
			// Instantiate the type class.
			String typeString = (String) entry.get("type");
			Class<?> typeClass;
			try {
				typeClass = Class.forName(typeString);
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to instantiate type class [" + typeString + "]", e);
			}
			
			// Type class must implement Injectable.
			if (!Injectable.class.isAssignableFrom(typeClass)) {
				throw new VariantInternalException("Type [" + typeString + "] must extend [" + Injectable.class.getName() + "]");
			}
			
			// Instantiate the implementation
			String implString = (String) entry.get("impl");
			Object implObject;
			try {
				Class<?> implClass = Class.forName(implString);
				implObject = implClass.newInstance();
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to instantiate implementation class [" + implString + "]", e);
			}

			if (!typeClass.isInstance(implObject)) 
				throw new VariantInternalException("Class [" + implString + "] must be of type [" + typeString + "]");
			
			// Initialize the implementation.
			Injectable implInjectable = (Injectable) implObject;
			implInjectable.init(core, (Map<String, Object>)entry.get("init"));
			
			// Store in implMap, keyed by type
			result.put((Class<Injectable>) typeClass, implInjectable);
		}
		
		// Single assignment is thread safe.
		implMap = result;
	}
	
	public static<T extends Injectable> T inject(Class<T> clazz, VariantCore core) {
		if (implMap == null) lazyInit(core);
		return null;
	}
	
}
