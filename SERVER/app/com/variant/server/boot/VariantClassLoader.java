package com.variant.server.boot;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
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
			// If we were given the init argument, wrap it as a proper Config object rooted in "init"
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
	
		LOG.debug("Instantiated object of type [" + result.getClass().getCanonicalName() + "]");
		
		return result;
	}
}
