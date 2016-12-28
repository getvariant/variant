package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.ParserError.NO_STATES;
import static com.variant.core.schema.parser.ParserError.STATE_NAME_DUPE;
import static com.variant.core.schema.parser.ParserError.STATE_NAME_INVALID;
import static com.variant.core.schema.parser.ParserError.STATE_NAME_MISSING;
import static com.variant.core.schema.parser.ParserError.STATE_PARAMS_NOT_OBJECT;
import static com.variant.core.schema.parser.ParserError.STATE_UNSUPPORTED_PROPERTY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.variant.core.exception.InternalException;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.StateImpl;

/**
 * Parse the STATES clause.
 * @author Igor
 *
 */
public class StatesParser implements Keywords {
			
	/**
	 * Parse the STATES clause.
	 * @param statesObject
	 * @param response 
	 */
	@SuppressWarnings("unchecked")
	static void parse(Object statesObject, ParserResponseImpl response) {

		try {

			List<Map<String, ?>> rawStates = (List<Map<String, ?>>) statesObject;
			
			if (rawStates.size() == 0) {
				response.addMessage(NO_STATES);
			}
			
			for (Map<String, ?> rawState: rawStates) {
				State state = parseState(rawState, response);
				if (state != null && !((SchemaImpl) response.getSchema()).addState(state)) {
					response.addMessage(STATE_NAME_DUPE, state.getName());
				}
			}
		}
		catch (Exception e) {
			throw new InternalException(e);
		}
		
	}
	
	/**
	 * 
	 * @param view
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	private static State parseState(Map<String, ?> rawState, final ParserResponseImpl response) {
		
		String name = null;
		boolean nameFound = false;
		Map<String, String> params = null;
		
		// Pass 1: figure out the name.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(STATE_NAME_INVALID);
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(STATE_NAME_INVALID);
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(STATE_NAME_MISSING);
			}
			return null;
		}
		
		// Pass 2: Parse parameters, if we have the name.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				Object paramsObject = entry.getValue();
				if (! (paramsObject instanceof Map)) {
					response.addMessage(STATE_PARAMS_NOT_OBJECT, name);
				}
				params = (Map<String, String>) paramsObject;
			}
			else {
				response.addMessage(STATE_UNSUPPORTED_PROPERTY, entry.getKey(), name);
			}
		}
		
		if (params == null) params = new HashMap<String,String>();

		return new StateImpl(response.getSchema(), name, params);
	}

}
