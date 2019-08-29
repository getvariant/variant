package com.variant.server.util;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.variant.core.util.ReflectUtils;

/**
 * Custom class loader to look in the ext/ directory in addition to the managed dependencies in lib/
 * The superclass does the delegation to the parent.
 *
 */
public class ClassUtil  {

	private static final Logger LOG = LoggerFactory.getLogger(ClassUtil.class);
	
	/**
	 * Instantiate a class with a given name.
	 * @return null if proper constructor could not be found, i.e. nullary if initArg was null,
	 *         or the single arg constructor of type ConfigObject otherwise.
	 */
	public static Object instantiate(String className, Optional<String> init) throws Exception {

		// Java reflection API does not understand optionals, so translate.
		Config config  = !init.isPresent() || init.get().equals("null") ? null : ConfigFactory.parseString(init.get()); 
	   
		Object result = ReflectUtils.instantiate(className, Config.class, config);
		
		if (LOG.isTraceEnabled())
			LOG.trace("Instantiated object of type [" + result.getClass().getCanonicalName() + "]");
		
		return result;
	}
}
