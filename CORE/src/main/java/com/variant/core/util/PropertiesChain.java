package com.variant.core.util;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;

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
	private String expandVariables(String input) {
		
		if (input == null) return null;
		
		StringBuilder result = new StringBuilder();
		
		StringBuilder var = null;
		boolean inPattern = false;
		
		for (int i = 0; i < input.length(); i++) {
			
			char c = input.charAt(i);
			
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
				if (c == '$' && i < input.length() - 1 && input.charAt(i+1) == '{') {
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
		
		if (inPattern) throw new RuntimeException("Unable to expand property value '" + input + "'");
		
		return result.toString();
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
	public void add(Properties props) {
		propsChain.add(props);
	}
	
	/**
	 * Find the first occurrence of key, walking props chain from most recent backwards.
	 * @param key
	 * @return
	 */
	public String getProperty(String key) {

		if (propsChain.size() == 0) throw new IllegalStateException("Empty properties chain.");

		String result = null;
		ListIterator<Properties> iter = propsChain.listIterator(propsChain.size());
		while (iter.hasPrevious()) {
			if ((result = iter.previous().getProperty(key)) != null) break;
		}
		
		return expandVariables(result);
	}

}
