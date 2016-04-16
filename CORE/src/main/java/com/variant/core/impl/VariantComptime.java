package com.variant.core.impl;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.Variant;
import com.variant.core.exception.VariantInternalException;


public class VariantComptime {
	
	public static final Logger LOG = LoggerFactory.getLogger(Variant.class);
	
	private String coreVersion = null;
	private String component = "NONE";
	private String componentVersion = "NONE";
	
	private boolean isComponentRegistered = false;
	
	/**
	 * Package instantiation
	 * @throws Exception
	 */
	VariantComptime() throws Exception {	        

		// If we're called by a JUnit test, the version is passed in the system variable by the surefire Maven plugin.
		coreVersion = System.getProperty("variant.version");
		
		if (coreVersion == null) {
			
		    // Otherwise, if we're running off the core jar file, Maven will put the version into META-INF.
	        Properties mavenProps = new Properties();
	        InputStream is = VariantComptime.class.getResourceAsStream("/META-INF/maven/com.variant/variant-core/pom.properties");
	        if (is != null) {
	        	mavenProps.load(is);
	        	coreVersion = mavenProps.getProperty("version");
	        }
		}
		
		if (coreVersion == null) coreVersion = "?.?.?";
	}
	
	public void registerComponent(String component, String version) {
		
		if (isComponentRegistered) throw new VariantInternalException(
				String.format("Already registered component [%s] [%s]", this.component, this.componentVersion));
		
		this.component = component;
		this.componentVersion = version;
		LOG.info(String.format("Registered component %s %s, © 2015-16 getvariant.com.", component, component));
	}
	
	public String getCoreVersion() { return coreVersion; }
	public String getComponent() { return component; }
	public String getComponentVersion() { return componentVersion; }
}
