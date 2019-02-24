package com.variant.server.util;
/*
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

/**
 * Custom class loader to look in the ext/ directory in addition to the managed dependencies in lib/
 * The superclass does the delegation to the parent.
 *
 *  DON'T THINK WE NEED IT.
public class VariantClassLoader extends URLClassLoader {

	private static final Logger LOG = LoggerFactory.getLogger(VariantClassLoader.class);
	
	/**
	 * Build an array of custom URLs we want to include in the path.
	 * @return
	 *
	private static URL[] getCustomUrls() {

		// Let tests override the location of the ext directory
		String extDirName = System.getProperty("variant.ext.dir");
		if (extDirName == null) extDirName = System.getProperty("user.dir") + "/ext";
		
		File extDir = new File(extDirName);
         
	   // if ext directory doesn't exist, we assume to run not in production.
	   if (!extDir.exists() || !extDir.isDirectory()) {
		   LOG.error("Extension directory [" + extDir.getAbsolutePath() + "] does not exist.");
		   return new URL[0];
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
			   if (LOG.isTraceEnabled())
				   LOG.trace("Found class or jar file " + urls[i].toExternalForm());
		   }
		   catch (Exception e) {
			   throw new ServerException.Internal("Unable to initialize Variant class loader");
		   }
	   }

	   LOG.debug("Instantiated class loader from [" + extDir.getAbsolutePath() + "]");
	   return (urls);

	}
	
	/**
	 * package visibility constructor.
	 * @return
	 *
	public VariantClassLoader() {
		super(getCustomUrls());
	}

	/**
	 * Instantiate a class with a given name.
	 * @return null if proper constructor could not be found, i.e. nullary if initArg was null,
	 *         or the single arg constructor of type ConfigObject otherwise.
	 *
	public Object instantiate(String className, String initArg) throws Exception {

		Class<?> cls = super.loadClass(className);

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
*/