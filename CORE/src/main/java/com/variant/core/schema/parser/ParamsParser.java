package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.ELEMENT_NOT_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.NAME_INVALID;
import static com.variant.core.schema.parser.error.SemanticError.NAME_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_LIST;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_STRING;
import static com.variant.core.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.variant.core.impl.CoreException;
import com.variant.core.schema.parser.error.SemanticError.Location;
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
	static Map<String, String> parse(Object paramsObject, Location paramsLocation, ParserResponse response) {		
		
		LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
		
		try {
			List<?> rawParams = (List<?>) paramsObject;
									
			int index = 0;
			for (Object rawParam: rawParams) {
				
				Pair<String,String> param = parseParam(rawParam, paramsLocation.plusIx(index++), response);
				
				if (param != null) result.put(param._1(), param._2());

			}
		}
		catch (ClassCastException e) {
			response.addMessage(paramsLocation, PROPERTY_NOT_LIST, KEYWORD_PARAMETERS);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
		return result;
	}

	/**
	 * Parse an individual life-cycle hook definition
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
			response.addMessage(paramLocation, ELEMENT_NOT_OBJECT, KEYWORD_PARAMETERS);
			return null;
		}
		
		for (Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(paramLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(paramLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
					}
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_VALUE)) {
				Object valueObject = entry.getValue();
				if (valueObject != null && ! (valueObject instanceof String)) {
					response.addMessage(paramLocation.plusProp(KEYWORD_VALUE), PROPERTY_NOT_STRING, KEYWORD_VALUE);
				}
				else {
					value = (String) valueObject;
				}
			}
			else {
				response.addMessage(paramLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}

		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(paramLocation, NAME_MISSING);
			}
			return null;
		}
	
		return new Pair<String,String>(name, value);
	}
}
