package com.variant.core.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;

import com.variant.core.VariantInternalException;

/**
 * 
 * @author Igor
 *
 */
public class PropertiesChain  {
	
	private ArrayList<Properties> propsChain = new ArrayList<Properties>();
	
	/**
	 * Expand ${...} variables.
	 * Variables may refer to environment vars.
	 * TODO possibly extend to referencing other props that have already been set, like Ant build pros.  Problem: circular references.
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

	/**
	 * Find the first occurence of key, walking props chain from most recent backwards.
	 * @param key
	 * @return
	 */
	private String getProperty(String key) {

		if (propsChain.size() == 0) throw new IllegalStateException("Empty properties chain.");

		String result = null;
		ListIterator<Properties> iter = propsChain.listIterator(propsChain.size());
		while (iter.hasPrevious()) {
			if ((result = iter.previous().getProperty(key)) != null) break;
		}
		return result;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	public PropertiesChain() { }
	
	/**
	 * Add a new properties object by reading it from class path resource.
	 * @param fileName
	 * @return
	 */
	public void addFromResource(String fileName) {
		
		InputStream is = PropertiesChain.class.getResourceAsStream(fileName);
		if (is == null) {
			throw new RuntimeException("Classpath resource by the name '" + fileName + "' does not exist.");
		}
				
		try {
			Properties props = new Properties();
			props.load(is);
			propsChain.add(props);
		} catch (Throwable t) {
			throw new RuntimeException("Unable to load classpath resource by the name '" + fileName + "'", t);
		}
	}
			
	/**
	 * String value
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		String result = getProperty(key);
		if (result == null) throw new VariantInternalException("Undefined system property [" + key + "]");
		return expandVariables(result);
	}

	/**
	 * Integer value
	 * @param key
	 * @return
	 */
	public Integer getInteger(String key) {
		return Integer.parseInt(getString(key));
	}

	/**
	 * Boolean value
	 * @param key
	 * @return
	 */
	public Boolean getBoolean(String key) {
		return Boolean.parseBoolean(getString(key));
	}
		

}
