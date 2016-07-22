package com.variant.core.ext;


/**
 * Expects key "path" present in the map
 * and matches by exact comparison.
 * 
 * @author Igor
 *
 *
public class StateSelectorPathString  {

	//@Override
	public State select(Map<String,String> parameters, Collection<State> states) {
		String path = parameters.get("path");
		for (State s: states) {
			if (s.getParameterMap().get("path").equals(path)) return s;
		}
		return null;

	}
		
}
*/