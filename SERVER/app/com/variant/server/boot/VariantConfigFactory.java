package com.variant.server.boot;

import static com.variant.core.exception.RuntimeError.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.exception.RuntimeError.CONFIG_FILE_NOT_FOUND;
import static com.variant.core.exception.RuntimeError.CONFIG_RESOURCE_NOT_FOUND;

import java.io.InputStreamReader;

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

	public static final String CONFIG_RESOURCE = "/variant.config";
	public static final String SYSPROP_CONFIG_RESOURCE = "variant.config.resource";
	public static final String SYSPROP_CONFIG_FILE = "varaint.config.file";

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
			}
			catch (Exception e) {
				throw new RuntimeErrorException(CONFIG_RESOURCE_NOT_FOUND, e, resName);
			}
		}
		else if (fileName != null) {
			try {
				InputStreamReader reader = new InputStreamReader(VariantIoUtils.openFileAsStream(resName));
				result = ConfigFactory.parseReader(reader);
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
			}
			catch (Exception e) {
				// Doesn't have to be there.
			}
		}
		
		return result;
	}
}
