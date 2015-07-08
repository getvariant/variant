package com.variant.core.conf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;


public class ApplicationProperties {

	private static List<Properties> configs = null;

	/**
	 * Load properties file from class path.
	 * classes can use it too.
	 * @param fileName
	 * @return
	 */
	private static Properties loadFromClasspath(String fileName) {
		InputStream is = ApplicationProperties.class.getResourceAsStream(fileName);
		if (is == null) {
			throw new RuntimeException("Classpath resource by the name '" + fileName + "' does not exist.");
		}
		Properties result = new Properties();
		try {
			result.load(is);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to load classpath resource by the name '" + fileName + "'", t);
		}
		return result;
	}
	

	/**
	 * Constructor. Enforce at least one param.
	 * @param A series of class path property files to be consulted, from left to right.
	 *        At least one of these names must be found on the class path.
	 */
	ApplicationProperties(String first, String... rest) {
		
		// concatenate first and rest into one array.
		String[] names = ArrayUtils.addAll(new String[] {first}, rest);
		
		configs = new ArrayList<Properties>();
		
		for (String name: names) {
			try {
				configs.add(loadFromClasspath(name));
			}
			catch (RuntimeException rte) {
				// Wasn't found on the class path.  Not a problem
				// so long as there's at least one of these found
			}
		}
		
		if (configs.size() == 0) {
			throw new RuntimeException(
					"Unable to find any properties files on the class path. " +
					"Looked for files: " + ArrayUtils.toString(names));
		}
	}

	/**
	 * Get a property from property files.
	 * Scan properties left to right and return first one found.
	 * @param key
	 */
	private static String getProperty(String key) {
		String result = null;
		for (Properties config: configs) {
			if ((result = config.getProperty(key)) != null) break;
		}
		
		if (result == null) throw new RuntimeException ("Unable to obtain value for property " + key);
		
		return result;
	}
	
	/**
	 * Clear text string value
	 * @param key
	 * @return
	 */
	protected String getString(String key) {
		return expandVariables(getProperty(key));
	}

	/**
	 * Expand ${...} variables.
	 * @param string
	 * @return
	 */
	private String expandVariables(String string) {
		StringBuilder result = new StringBuilder();
		
		StringBuilder var = null;
		boolean inPattern = false;
		
		for (int i = 0; i < string.length(); i++) {
			
			char c = string.charAt(i);
			
			if (inPattern) {
				if (c == '}') {
					// We  have the variable name
					String varName = var.toString();
					String varValue = System.getenv(varName);
					result.append(varValue == null ? ("${" + varName + "}") : varValue);
					inPattern = false;
					continue;
				}
				else {
					var.append(c);
				}
			}
			else {
				if (c == '$' && i < string.length() - 1 && string.charAt(i+1) == '{') {
					inPattern = true;
					i++;
					var = new StringBuilder();
					continue;
				}
				else {
					result.append(c);
				}
			}
		}
		
		if (inPattern) throw new RuntimeException("Unable to expand property value '" + string + "'");
		
		return result.toString();
	}
	
	protected boolean getBoolean(String key) {
		return Boolean.parseBoolean(getProperty(key));
	}

    protected int getInt(String key) {
        return Integer.parseInt(getProperty(key));
    }
    
}
