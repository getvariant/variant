package com.variant.server.util;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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

		Class<?> cls = Class.forName(className);

		Object result = null;
		
		// No init property or 'init':null
		if (initArg == null || initArg.equals("null")) {
			
			// First look for the nullary constructor
			Constructor<?> constructor = null;
			try {
				constructor = cls.getConstructor();
				result = constructor.newInstance();
			}
			catch (NoSuchMethodException e) {
				// Not provided. May be okay.
			}
			
			if (constructor == null) {
				// Look for constructor which takes a single argument of type Config
				try {
					constructor = cls.getConstructor(Config.class);
					result = constructor.newInstance((Object)null);
		        }
				catch (NoSuchMethodException e) {
					return null;
				}
			}
		
		}
		else {

			// If we were given the init argument, parse it as Config.
			Config config  = ConfigFactory.parseString(initArg); 
			
			// and pass it to the constructor that takes it (must be provided)
			try {
				Constructor<?> constructor = cls.getConstructor(Config.class);
				result = constructor.newInstance(config);
			}
			catch (NoSuchMethodException e) {
				return null;
			}
		}
	
		if (LOG.isTraceEnabled())
			LOG.trace("Instantiated object of type [" + result.getClass().getCanonicalName() + "]");
		
		return result;
	}
}
