package com.variant.server.util;

import static com.variant.core.impl.CommonError.CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN;
import static com.variant.core.impl.CommonError.CONFIG_FILE_NOT_FOUND;
import static com.variant.core.impl.CommonError.CONFIG_RESOURCE_NOT_FOUND;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.variant.core.impl.CoreException;
import com.variant.core.util.IoUtils;
import com.variant.core.util.Tuples.Pair;

/**
 * Variant Configuration. A Typesafe Config based implementation.
 * See https://github.com/typesafehub/config for details.
 * 
 * 
 * @author Igor Urisman
 */
public class ConfigLoader {

	private static final String SYSPROP_CONFIG_RESOURCE = "variant.config.resource";
	private static final String SYSPROP_CONFIG_FILE = "variant.config.file";

	private static final String FORMAT_EXCEPTION = "Unable to open [%s]";
	private static final String FORMAT_RESOURCE_FOUND = "Found %s config resource [%s] as [%s]";
	private static final String FORMAT_RESOURCE_NOT_FOUND = "Could not find %s config resource [%s]";
	private static final String FORMAT_FILE_FOUND = "Found config file [%s]";

	private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);	
	
	/**
	 * Load the configuration from the runtime environment. Both client and
	 * server follow the same semantics:
	 * 1. If /variant.config is present on the classpath, it is merged with the default to produce interim.
	 * 2. If -Dvariant.config.resource given, it must exist and is merged with interim. 
	 * 3. if -Dvariant.config.file is given, it must exist and is merged with interim.
	 * 4. It's a user error to provide both 2. and 3. 
     * 
	 * @return an object of type {@link Config}. 
	 */
	public static Config load(String resourceName, String defaultResourceName) {

		// Check that we weren't given two overrides.
		String overrideResourceName = System.getProperty(SYSPROP_CONFIG_RESOURCE);
		String overrideFileName = System.getProperty(SYSPROP_CONFIG_FILE);
		
		if (overrideResourceName != null && overrideFileName!= null) {
			throw new CoreException.User(CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN);
		}

		// 0. Default config must exist.
		Pair<InputStream, String> defaultStream = null;
		
		try{
			defaultStream = IoUtils.openResourceAsStream(defaultResourceName);		
		}
		catch (Exception e) {
			throw new CoreException.Internal(String.format(FORMAT_EXCEPTION, defaultResourceName), e);
		}

		if (defaultStream == null) {
			throw new CoreException.Internal((String.format(FORMAT_RESOURCE_NOT_FOUND, "default", defaultResourceName)));
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format(FORMAT_RESOURCE_FOUND, "default", defaultResourceName, defaultStream._2()));
		}
		
		Config defaultConfig = ConfigFactory.parseReader(new InputStreamReader(defaultStream._1()));
				
		// 1. /variant.conf, if exists.
		Config interimConfig = defaultConfig;
		try {
			Pair<InputStream, String> res = IoUtils.openResourceAsStream(resourceName);
			if (res == null) {
				LOG.info(String.format(FORMAT_RESOURCE_NOT_FOUND, "", resourceName));
			}
			else {
				LOG.info(String.format(FORMAT_RESOURCE_FOUND, "", resourceName, res._2()));
				interimConfig = ConfigFactory.parseReader(new InputStreamReader(res._1())).withFallback(defaultConfig);
			}
		}
		catch (Exception e) {
			throw new CoreException.Internal(String.format(FORMAT_EXCEPTION, resourceName), e);
		}			

		Config result = interimConfig;
		
		// 2. Override file may have been given as resource.
		if (overrideResourceName != null) {
			try {
				Pair<InputStream, String> res = IoUtils.openResourceAsStream(overrideResourceName);
				if (res == null) {
					throw new CoreException.User(CONFIG_RESOURCE_NOT_FOUND, overrideResourceName);				
				}
				else {
					LOG.info(String.format(FORMAT_RESOURCE_FOUND, "", overrideResourceName, res._2()));
					result = ConfigFactory.parseReader(new InputStreamReader(res._1())).withFallback(interimConfig);
				}
			}
			catch (Exception e) {
				throw new CoreException.User(CONFIG_RESOURCE_NOT_FOUND, overrideResourceName);
			}
		}
		
		// 3. Override file may have been given as file
		else if (overrideFileName != null) {

		   InputStream is = null;
		   
			try {
				is = IoUtils.openFileAsStream(overrideFileName);
				if (is == null) {
					throw new CoreException.User(CONFIG_FILE_NOT_FOUND, overrideFileName);
				}
         }
         catch (Exception e) {
            throw new CoreException.User(CONFIG_FILE_NOT_FOUND, overrideFileName);
         }

			LOG.info(String.format(FORMAT_FILE_FOUND, overrideFileName));
			result = ConfigFactory.parseReader(new InputStreamReader(is)).withFallback(interimConfig);
		
		}

		// This seems a hack but I can't find a way to do it cleanly.
		// Override anything in the result with system properties, if set.
		HashMap<String, Object> sysPropsOverride = new HashMap<String, Object> ();
		for (Map.Entry<Object, Object> entry: System.getProperties().entrySet()) {
			if (result.hasPath(entry.getKey().toString())) {
				sysPropsOverride.put(entry.getKey().toString(), entry.getValue());
			}
		}

		return ConfigFactory.parseMap(sysPropsOverride).withFallback(result).resolve();
	}
	
}
