package com.variant.core.util.inject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.VariantCore;
import com.variant.core.util.VariantIoUtils;

/**
 * Implementation injection, deferred to run time.
 * Lazily parse /variant/injector.json from the classpath once, upon first invocation of inject().
 * Defer instantiating implementation until injection time, because it depends on the core instance.
 * 
 * @author Igor
 * @since 0.6
 */
public class Injector {

	private static final Logger LOG = LoggerFactory.getLogger(Injector.class);
	private static final String DEFAULT_CONFIG_RESOURCE_NAME = "/variant/injector.json";
	private static HashMap<Class<? extends Injectable>, Entry> entryMap = null;
	
	private static String configName = DEFAULT_CONFIG_RESOURCE_NAME;
	
	private static class Entry {
		Class<? extends Injectable> type;
		String implName;
		Map<String, Object> init;
		
		/**
		 * 
		 * @param type
		 * @param implName
		 * @param init
		 */
		Entry(Class<? extends Injectable> type, String implName, Map<String, Object> init) {
			this.type = type;
			this.implName = implName;
			this.init = init;
		}
		
		/**
		 * 
		 * @return
		 */
		Injectable newInstance(VariantCore core) {

			// Create new instance
			Object implObject;
			try {
				Class<?> implClass = Class.forName(implName);
				implObject = implClass.newInstance();
			}
			catch (Exception e) {
				throw new VariantInternalException("Unable to instantiate implementation class [" + implName + "]", e);
			}

			if (!type.isInstance(implObject)) 
				throw new VariantInternalException("Class [" + implName + "] must be of type [" + type.getName() + "]");
						
			// Initialize new instance.
			Injectable implInjectable = (Injectable) implObject;
			implInjectable.init(core, init);
			
			return implInjectable;
		}
	}
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private static void lazyInit() {
		
		InputStream configStream = VariantIoUtils.openResourceAsStream(configName);
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		List<Map<String, ?>> parseTree = null;

		// Parse the entire config in memory.
		try {
			parseTree = jacksonDataMapper.readValue(configStream, List.class);
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to parse injector config [" + configName + "]", e);
		}
		
		HashMap<Class<? extends Injectable>, Entry> result = new HashMap<Class<? extends Injectable>, Entry>();
		
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
			
			// Store in implMap, keyed by type
			Entry mapEntry = new Entry((Class<Injectable>) typeClass, (String) entry.get("impl"), (Map<String, Object>)entry.get("init"));
			result.put((Class<Injectable>)typeClass, mapEntry);
		}
		
		// Single assignment is thread safe.
		entryMap = result;
	}
	
    //---------------------------------------------------------------------------------------------//
	//                                           PUBLIC                                            //
	//---------------------------------------------------------------------------------------------//	

	/**
	 * 
	 * @param configName
	 */
	public static void setConfigNameAsResource(String name) {
		configName = name;
		entryMap = null;
	}

	/**
	 * 
	 */
	public static void restoreDefaultConfig() {
		configName = DEFAULT_CONFIG_RESOURCE_NAME;
		entryMap = null;
	}
	
	/**
	 * 
	 * @param clazz
	 * @param core
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static<T extends Injectable> T inject(Class<T> clazz, VariantCore core) {
		if (entryMap == null) lazyInit();
		T result = (T) entryMap.get(clazz).newInstance(core);
		LOG.info(String.format("Injected an instance of %s for type %s", result.getClass().getName(), clazz.getName()));
		return result;
	}
	
}
