package com.variant.core.impl;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.exception.VariantInternalException;


public class VariantComptime {
	
	public static final Logger LOG = LoggerFactory.getLogger(VariantComptime.class);
	
	public enum Component {SERVER, CLIENT}
	
	private String coreVersion = null;
	private Component component = null;
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
	
	public void registerComponent(Component component, String version) {
		
		if (isComponentRegistered) throw new VariantInternalException(
				String.format("Already registered component [%s] [%s]", this.component, this.componentVersion));
		
		this.component = component;
		this.componentVersion = version;
		if (LOG.isDebugEnabled())
			LOG.debug(String.format("Registered component [%s] [%s]", component, componentVersion));
	}
	
	public String getCoreVersion() { return coreVersion; }
	public Component getComponent() { return component; }
	public String getComponentVersion() { return componentVersion; }
}

