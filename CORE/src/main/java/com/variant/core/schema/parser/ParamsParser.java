package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.*;

import java.util.List;
import java.util.Map;

import com.variant.core.CoreException;
import com.variant.core.schema.parser.error.SemanticError.Location;
import com.variant.core.util.CaseInsensitiveMap;
import com.variant.core.util.Tuples.Pair;

/**
 * Hooks parser
 * @author Igor
 */

public class ParamsParser implements Keywords {
	
	/**
	 * Parse hooks list with the state scope. 
	 * @param hooksObject
	 * @param response
	 */
	static CaseInsensitiveMap<String> parse(Object paramsObject, Location paramsLocation, ParserResponse response) {		
		
		CaseInsensitiveMap<String> result = new CaseInsensitiveMap<String>();
		
		try {
			List<?> rawParams = (List<?>) paramsObject;
									
			int index = 0;
			for (Object rawParam: rawParams) {
				
				Pair<String,String> param = parseParam(rawParam, paramsLocation.plus(index++), response);
				
				if (param != null) result.put(param);
			}
		}
		catch (ClassCastException e) {
			response.addMessage(PARAMS_NOT_LIST, paramsLocation);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
		return result;
	}

	/**
	 * Parse an individual user hook definition
	 * @param rawHook
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Pair<String,String> parseParam(Object rawParam, Location paramLocation, ParserResponse response) {
		
		String name = null;
		String value = null;
		
		// Pass 1: figure out the name.
		boolean nameFound = false;
		
		Map<String, ?> rawMap;
		try {
			rawMap = (Map<String,?>) rawParam;
		}
		catch (ClassCastException e) {
			response.addMessage(PARAM_NOT_OBJECT, paramLocation);
			return null;
		}
		
		for (Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(PARAM_NAME_INVALID, paramLocation.plus(KEYWORD_NAME));
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(PARAM_NAME_INVALID, paramLocation.plus(KEYWORD_NAME));
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(PARAM_NAME_MISSING, paramLocation);
			}
			return null;
		}

		// Pass 2: figure out the value.
		for(Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				continue;
			} 
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_VALUE)) {
				Object valueObject = entry.getValue();
				if (! (valueObject instanceof String)) {
					response.addMessage(PARAM_VALUE_INVALID, paramLocation.plus(KEYWORD_VALUE), name);
				}
				else {
					value = (String) valueObject;
				}
			}
			else {
				response.addMessage(HOOK_UNSUPPORTED_PROPERTY, paramLocation, entry.getKey(), name);
			}
		}
	
		return new Pair<String,String>(name, value);
	}
}
