package com.variant.core.util;

import static com.variant.core.exception.CommonError.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.exception.CommonError.CONFIG_FILE_NOT_FOUND;
import static com.variant.core.exception.CommonError.CONFIG_RESOURCE_NOT_FOUND;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.variant.core.exception.CoreException;
import com.variant.core.util.Tuples.Pair;

/**
 * Variant Configuration. A Typesafe Config based implementation.
 * See https://github.com/typesafehub/config for details.
 * 
 * 
 * @author Igor Urisman
 */
public class VariantConfigLoader {

	private static final String SYSPROP_CONFIG_RESOURCE = "variant.config.resource";
	private static final String SYSPROP_CONFIG_FILE = "variant.config.file";

	private static final String FORMAT_EXCEPTION = "Unable to open [%s]";
	private static final String FORMAT_RESOURCE_FOUND = "Found %sconfig resource [%s] as [%s]";
	private static final String FORMAT_RESOURCE_NOT_FOUND = "Could not find %s config resource [%s]";
	private static final String FORMAT_FILE_FOUND = "Found config file [%s]";

	private static final Logger LOG = LoggerFactory.getLogger(VariantConfigLoader.class);

	private final String resourceName;
	private String defaultResourceName;
	
	public VariantConfigLoader(String resourceName, String defaultResourceName) {
		this.resourceName = resourceName;
		this.defaultResourceName = defaultResourceName;
	}
	
	/**
	 * Load the configuration from the runtime environment. Both client and
	 * server follow the same semantics. If -Dvariant.config.resource or 
	 * -Dvariant.config.file is given, load from that, otherwise, look for 
     * /variant.config as classpath resource.
     * 
	 * @return an object of type {@link Config}. 
	 */
	public Config load() {

		// Parse the default config - Must exist.
		
		Pair<InputStream, String> defaultConfig = null;
		
		try{
			defaultConfig = VariantIoUtils.openResourceAsStream(defaultResourceName);		
		}
		catch (Exception e) {
			throw new CoreException.Internal(String.format(FORMAT_EXCEPTION, defaultResourceName), e);
		}

		if (defaultConfig == null) {
			throw new CoreException.Internal((String.format(FORMAT_RESOURCE_NOT_FOUND, "default", defaultResourceName)));
		}

		LOG.debug(String.format(FORMAT_RESOURCE_FOUND, "default ", defaultResourceName, defaultConfig._2()));
		
		Config variantDefault = ConfigFactory.parseReader(new InputStreamReader(defaultConfig._1()));

		// Did we have a system property set? If so, it overrides the regular config.
		String resName = System.getProperty(SYSPROP_CONFIG_RESOURCE);
		String fileName = System.getProperty(SYSPROP_CONFIG_FILE);
		
		if (resName != null && fileName!= null) {
			throw new CoreException.User(CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);
		}
				
		Config result = ConfigFactory.empty();
		
		if (resName != null) {
			try {
				Pair<InputStream, String> res = VariantIoUtils.openResourceAsStream(resName);
				if (res == null) {
					throw new CoreException.User(CONFIG_RESOURCE_NOT_FOUND, resName);				
				}
				else {
					LOG.debug(String.format(FORMAT_RESOURCE_FOUND, "", resName, res._2()));
					result = ConfigFactory.parseReader(new InputStreamReader(res._1()));
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal(String.format(FORMAT_EXCEPTION, resName), e);
			}
		}
		else if (fileName != null) {
			try {
				InputStream is = VariantIoUtils.openFileAsStream(fileName);
				if (is == null) {
					throw new CoreException.User(CONFIG_FILE_NOT_FOUND, fileName);
				}
				else {
					LOG.debug(String.format(FORMAT_FILE_FOUND, fileName));
					result = ConfigFactory.parseReader(new InputStreamReader(is));
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal(String.format(FORMAT_EXCEPTION, fileName), e);
			}
		}
		else {
			try {
				Pair<InputStream, String> res = VariantIoUtils.openResourceAsStream(resourceName);
				if (res == null) {
					LOG.info(String.format(FORMAT_RESOURCE_NOT_FOUND, "", resourceName));
				}
				else {
					LOG.info(String.format(FORMAT_RESOURCE_FOUND, "", resourceName, res._2()));
					result = ConfigFactory.parseReader(new InputStreamReader(res._1()));
				}
			}
			catch (Exception e) {
				throw new CoreException.Internal(String.format(FORMAT_EXCEPTION, resourceName), e);
			}			
		}
		
		return result.withFallback(variantDefault);
	}
	
}
