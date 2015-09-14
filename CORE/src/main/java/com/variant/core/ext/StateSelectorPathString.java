package com.variant.core.ext;

import java.util.Collection;
import java.util.Map;

import com.variant.core.schema.State;

/**
 * Expects key "path" present in the map
 * and matches by exact comparison.
 * 
 * @author Igor
 *
 */
public class StateSelectorPathString /*implements StateSelector*/ {

	//@Override
	public State select(Map<String,String> parameters, Collection<State> states) {
		String path = parameters.get("path");
		for (State s: states) {
			if (s.getParameter("path").equals(path)) return s;
		}
		return null;

	}
		

}
