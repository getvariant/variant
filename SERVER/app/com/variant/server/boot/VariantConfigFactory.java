package com.variant.server.boot;

import static com.variant.core.exception.RuntimeError.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.exception.RuntimeError.CONFIG_FILE_NOT_FOUND;
import static com.variant.core.exception.RuntimeError.CONFIG_RESOURCE_NOT_FOUND;

import java.io.File;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.variant.core.exception.RuntimeErrorException;
import com.variant.core.util.VariantIoUtils;

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
				
		Config result = ConfigFactory.empty();
		
		if (resName != null) {
			try {
				InputStreamReader reader = new InputStreamReader(VariantIoUtils.openResourceAsStream(resName));
				result = ConfigFactory.parseReader(reader);
				LOG.debug(String.format("Found config resource [%s]", resName));
			}
			catch (Exception e) {
				throw new RuntimeErrorException(CONFIG_RESOURCE_NOT_FOUND, resName);
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
			// Override with /variant.props if supplied on classpath.
			try {
				InputStreamReader reader = new InputStreamReader(VariantIoUtils.openResourceAsStream(CONFIG_RESOURCE));
				result = ConfigFactory.parseReader(reader);
				LOG.info(String.format("Found config resource [%s]", CONFIG_RESOURCE));
			}
			catch (Exception e) {
				LOG.debug(String.format("Could NOT find config resource [%s]", CONFIG_RESOURCE));
			}
		}
		
		return result;
	}
}
