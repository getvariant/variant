package com.variant.server.boot;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.variant.server.api.ServerException;

public class VariantClassLoader {

	private static final Logger LOG = LoggerFactory.getLogger(VariantClassLoader.class);
	private final static ClassLoader instance = newInstance();   

	/**
	 * 
	 * @return
	 */
	private static ClassLoader newInstance() {
	   
		String extDirName = System.getProperty("user.dir") + "/ext";

		File extDir = new File(extDirName);
         
	   // if ext directory doesn't exist, we assume to run not in production.
	   if (!extDir.exists() || !extDir.isDirectory()) {
		   //TODO; inject play mode here so we only issue this error in produciton.
		   LOG.error("Extension directory [" + extDir.getAbsolutePath() + "] does not exist.");
		   return URLClassLoader.newInstance(new URL[0]);
	   }
      
	   // Construct list of all jars in the directory as URLs
	   File[] files = extDir.listFiles(
			   new FileFilter() {
               @Override public boolean accept(File file) { return file.isFile(); }   
            });
      
	   URL[] urls = new URL[files.length];
	   for (int i = 0; i < files.length; i++) {
		   try {
			   urls[i] = files[i].toURI().toURL();
		   }
		   catch (Exception e) {
			   throw new ServerException.Internal("Unable to initialize Variant class loader");
		   }
	   }
      return URLClassLoader.newInstance(urls);
	}

	/**
	 * Instantiate a class with a given name.
	 * @return null if proper constructor could not be found, i.e. nullary if initArg was null,
	 *         or the single arg constructor of type ConfigObject otherwise.
	 */
	public static Object instantiate(String className, String initArg) throws Exception {
		
		Class<?> cls = VariantClassLoader.instance.loadClass(className);

		Object result = null;
		
		if (initArg == null) {
			
			// First look for the nullary constructor
			Constructor<?> constructor = cls.getConstructor();
			if (constructor == null) {
				// Nullary constructor not provided.  
				// Ok, if there's one that take a single argument of type ConfigObject
				constructor = cls.getConstructor(ConfigObject.class);
				if (constructor == null) return null;
			}
			
			result = constructor.newInstance((Object[])null);
		}
		else {
			// If we were given the init argument, wrap it as a proper Config object rooted in "init"
			ConfigValue config  = ConfigFactory.parseString("{init:"  + initArg + "}").getValue("init"); 
			Constructor<?> constructor = cls.getConstructor(ConfigObject.class);
			if (constructor == null) return null;
			result = constructor.newInstance(config);
		}
	
		LOG.debug("Instantiated object of type [" + result.getClass().getCanonicalName() + "]");
		
		return result;
	}
}
