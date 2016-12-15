package com.variant.core.util.inject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.exception.RuntimeInternalException;
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
	private static final String[] DEFAULT_CONFIG_RESOURCE_NAMES = {
		"/com/variant/client/conf/injector.json", // Try client first 
		"/com/variant/core/conf/injector.json"};  // then core.
	
	private static HashMap<Class<? extends Injectable>, Entry> entryMap = null;
	
	private static String[] configNames = DEFAULT_CONFIG_RESOURCE_NAMES;
	
	private static class Entry {
		Class<? extends Injectable> impl;
		Map<String, Object> init;
		
		/**
		 * 
		 * @param type
		 * @param implName
		 * @param init
		 */
		Entry(Class<? extends Injectable> impl, Map<String, Object> init) {
			this.impl = impl;
			this.init = init;
		}
		
		/**
		 * 
		 * @return
		 */
		Injectable newInstance() {

			// Create new instance
			Injectable result;
			try {
				result = impl.newInstance();
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to instantiate implementation class [" + impl.getName() + "]", e);
			}
						
			result.init(init);
			
			return result;
		}
	}
	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	private static void lazyInit() {
		
		// This will open the first one found to exist
		InputStream configStream = VariantIoUtils.openResourceAsStream(configNames);
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		List<Map<String, ?>> parseTree = null;

		// Parse the entire config in memory.
		try {
			parseTree = jacksonDataMapper.readValue(configStream, List.class);
		}
		catch (Exception e) {
			throw new RuntimeInternalException("Unable to parse injector config [" + configNames + "]", e);
		}
		
		HashMap<Class<? extends Injectable>, Entry> result = new HashMap<Class<? extends Injectable>, Entry>();
		
		// for each config entry
		for (Map<String, ?> entry: parseTree) {
			
			// Instantiate the type class and check that it is assignable to Injectable.
			String typeString = (String) entry.get("type");
			Class<?> typeClass;
			try {
				typeClass = Class.forName(typeString);
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to instantiate type class [" + typeString + "]", e);
			}
			
			// Type class must implement Injectable.
			if (!Injectable.class.isAssignableFrom(typeClass)) {
				throw new RuntimeInternalException("Type [" + typeString + "] must extend [" + Injectable.class.getName() + "]");
			}
			
			// Instantiate the impl class and check that it is assignable to type class.
			String implString = (String) entry.get("impl");
			Class<?> implClass;
			try {
				implClass = Class.forName(implString);
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to instantiate implementation class [" + implString + "]", e);
			}

			if (!typeClass.isAssignableFrom(implClass)) 
				throw new RuntimeInternalException("Class [" + implString + "] must be of type [" + typeClass.getName() + "]");

			// Store in implMap, keyed by type
			Entry mapEntry = new Entry((Class<Injectable>) implClass, (Map<String, Object>)entry.get("init"));
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
		configNames = new String[] {name};
		entryMap = null;
	}

	/**
	 * 
	 */
	public static void restoreDefaultConfig() {
		configNames = DEFAULT_CONFIG_RESOURCE_NAMES;
		entryMap = null;
	}
	
	/**
	 * 
	 * @param clazz
	 * @param core
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static<T extends Injectable> T inject(Class<T> clazz) {
		if (entryMap == null) lazyInit();
		T result = (T) entryMap.get(clazz).newInstance();
		LOG.info(String.format("Injected an instance of [%s] for type [%s]", result.getClass().getName(), clazz.getName()));
		return result;
	}
	
}
