package com.variant.core.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import com.variant.core.util.Tuples.Pair;

/**
 * 
 * @author Igor
 *
 */
public class PropertiesChain  {
	
	private ArrayList<Properties> propsList = new ArrayList<Properties>();
	private ArrayList<String> sourceList = new ArrayList<String>();
	
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
	 * Add a new properties object by reading it from class path resource, to the head of the queue.
	 * Its contents override the rest of the chain.
	 * @param fileName
	 * @return
	 */
	public void overrideWith(Properties props, String source) {
		propsList.add(0, props);
		sourceList.add(0, source);
	}
	
	/**
	 * Find the first occurrence of key, walking props chain from most recent backwards.
	 * @param key
	 * @return A pair: string value and its source
	 */
	public Pair<String, String> getProperty(String key) {

		Iterator<Properties> propsIter = propsList.iterator();
		Iterator<String> sourceIter = sourceList.iterator();
		while (propsIter.hasNext()) {
			String value = propsIter.next().getProperty(key);
			String source = sourceIter.next();
			if (value != null) {
				return new Pair<String, String>(expandVariables(value), source);
			}
		}
		
		return null;
	}

}
