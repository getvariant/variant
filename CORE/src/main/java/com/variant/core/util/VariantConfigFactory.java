package com.variant.core.util;

import static com.variant.core.exception.RuntimeError.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.exception.RuntimeError.CONFIG_FILE_NOT_FOUND;
import static com.variant.core.exception.RuntimeError.CONFIG_RESOURCE_NOT_FOUND;

import java.io.InputStreamReader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.variant.core.exception.RuntimeErrorException;
import com.variant.core.exception.RuntimeInternalException;

/**
 * Variant Configuration. A Typesafe Config based implementation.
 * See https://github.com/typesafehub/config for details.
 * 
 * 
 * @author Igor Urisman
 */
public class VariantConfigFactory {

	public static final String CONFIG_RESOURCE = "/variant.conf";
	public static final String SYSPROP_CONFIG_RESOURCE = "variant.config.resource";
	public static final String SYSPROP_CONFIG_FILE = "variant.config.file";

	private static final Logger LOG = LoggerFactory.getLogger(VariantConfigFactory.class);
	private VariantConfigFactory() {}
	
	/**
	 * Load the configuration from the runtime environment. Both client and
	 * server follow the same semantics. If -Dvariant.config.resource or 
	 * -Dvariant.config.file is given, load from that, otherwise, look for 
     * /variant.config as classpath resource.
     * 
	 * @return an object of type {@link Config}. 
	 */
	public static Config load() {

		String resName = System.getProperty(SYSPROP_CONFIG_RESOURCE);
		String fileName = System.getProperty(SYSPROP_CONFIG_FILE);
		
		if (resName != null && fileName!= null) {
			throw new RuntimeErrorException(CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);
		}
				
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Config result = ConfigFactory.empty();
		
		if (resName != null) {
			try {
				URL res = cl.getResource(resName);
				if (res == null) {
					throw new RuntimeErrorException(CONFIG_RESOURCE_NOT_FOUND, resName);				
				}
				else {
					LOG.debug(String.format("Found config resource [%s] as [%s]", resName, res.getFile()));
					result = ConfigFactory.parseReader(new InputStreamReader(res.openStream()));
				}
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to open resource [" + resName + "]", e);
			}
		}
		else if (fileName != null) {
			try {
				InputStreamReader reader = new InputStreamReader(VariantIoUtils.openFileAsStream(fileName));
				result = ConfigFactory.parseReader(reader);
				LOG.debug(String.format("Found config file [%s]", fileName));
			}
			catch (Exception e) {
				throw new RuntimeErrorException(CONFIG_FILE_NOT_FOUND, e, fileName);
			}
		}
		else {
			try {
				URL res = cl.getResource(CONFIG_RESOURCE);
				if (res == null) {
					LOG.debug(String.format("Could NOT find config resource [%s]", CONFIG_RESOURCE));
				}
				else {
					LOG.info(String.format("Found config resource [%s] as [%s]", CONFIG_RESOURCE, res.getFile()));
					result = ConfigFactory.parseReader(new InputStreamReader(res.openStream()));
				}
			}
			catch (Exception e) {
				throw new RuntimeInternalException("Unable to open resource [" + CONFIG_RESOURCE + "]", e);
			}			
		}
		
		return result;
	}
}
