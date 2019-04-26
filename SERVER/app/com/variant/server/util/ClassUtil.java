package com.variant.server.util;

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
	public static Object instantiate(String className, String initArg) throws Exception {

		Config config  = initArg == null || initArg.equals("null") ? null : ConfigFactory.parseString(initArg); 

		Object result = ReflectUtils.instantiate(className, Config.class, config);
		
		if (LOG.isTraceEnabled())
			LOG.trace("Instantiated object of type [" + result.getClass().getCanonicalName() + "]");
		
		return result;
	}
}
