package com.variant.core.impl;

import java.io.InputStream;
import java.util.Properties;

import com.variant.core.Variant;

public class RuntimeService {

	public static String getVersion() {
		
		String result = null;

		// If we're called by a JUnit test, the version is passed in the system variable by
		// the surefire Maven plugin.
		result = System.getProperty("variant.version");
		if (result != null) return result;
		
	    // Otherwise, if we're running off the core jar file, Maven will put the version into META-INF.
	    try {
	        Properties mavenProps = new Properties();
	        InputStream is = Variant.class.getResourceAsStream("/META-INF/maven/com.variant.core/variant-core/pom.properties");
	        if (is != null) {
	        	mavenProps.load(is);
	        	return mavenProps.getProperty("version", "");
	        }
	    } catch (Exception e) {
	        // ignore
	    }

	    
	    /* fallback to using Java API ?
	    if (version == null) {
	        Package aPackage = Variant.class.getPackage();
	        if (aPackage != null) {
	            version = aPackage.getImplementationVersion();
	            if (version == null) {
	                version = aPackage.getSpecificationVersion();
	            }
	        }
	    }
	    */
	    
	    // If still nothing, we must be running under eclipse where none of the above applies.
	    return null;
	} 

}

