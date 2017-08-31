package com.variant.server.boot;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantClassLoader {

   private static final Logger LOG = LoggerFactory.getLogger(VariantClassLoader.class);

	public static ClassLoader newInstance() throws Exception {
	   
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
            }
      );
      
      URL[] urls = new URL[files.length];
      for (int i = 0; i < files.length; i++) {
         urls[i] = files[i].toURI().toURL();
      }
      return URLClassLoader.newInstance(urls);
	}


}
