package com.variant.core.config;

import java.io.InputStream;
import java.util.Properties;


public class ComptimeService {
	
	private static String coreVersion = null;
	private static String component = "NONE";
	private static String componentVersion = "NONE";
	
	public static void init() throws Exception {	        

		// If we're called by a JUnit test, the version is passed in the system variable by the surefire Maven plugin.
		coreVersion = System.getProperty("variant.version");
		
		if (coreVersion == null) {
			
		    // Otherwise, if we're running off the core jar file, Maven will put the version into META-INF.
	        Properties mavenProps = new Properties();
	        InputStream is = ComptimeService.class.getResourceAsStream("/META-INF/maven/com.variant/variant-core/pom.properties");
	        if (is != null) {
	        	mavenProps.load(is);
	        	coreVersion = mavenProps.getProperty("version");
	        }
		}
		
		if (coreVersion == null) coreVersion = "?.?.?";
	}
	
	public static void registerComponent(String component, String version) {
		ComptimeService.component = component;
		ComptimeService.componentVersion = version;
	}
	
	public static String getCoreVersion() { return coreVersion; }
	public static String getComponent() { return component; }
	public static String getComponentVersion() { return componentVersion; }
}

