package com.variant.core.schema.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserMessage;

import static com.variant.core.schema.parser.MessageTemplate.*;

/**
 * Parse the STATES clause.
 * @author Igor
 *
 */
public class StatesParser implements Keywords {

	private static final Logger LOG = LoggerFactory.getLogger(StatesParser.class);
			
	/**
	 * Parse the VIEWS clause.
	 * @param result
	 * @param viewsObject
	 */
	@SuppressWarnings("unchecked")
	static void parseStates(Object statesObject, ParserResponseImpl response) {

		try {

			List<Map<String, ?>> rawStates = (List<Map<String, ?>>) statesObject;
			
			if (rawStates.size() == 0) {
				response.addMessage(PARSER_NO_STATES);
			}
			
			for (Map<String, ?> rawState: rawStates) {
				State state = parseState(rawState, response);
				if (state != null && !((SchemaImpl) response.getSchema()).addState(state)) {
					response.addMessage(PARSER_STATE_NAME_DUPE, state.getName());
				}
			}
		}
		catch (Exception e) {
			ParserMessage err = response.addMessage(INTERNAL, e.getMessage());
			LOG.error(err.getMessage(), e);
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
					response.addMessage(PARSER_STATE_NAME_NOT_STRING);
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(PARSER_STATE_NAME_MISSING);
			}
			return null;
		}
		
		// Pass 2: Parse parameters, if we have the name.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				Object paramsObject = entry.getValue();
				if (! (paramsObject instanceof Map)) {
					response.addMessage(PARSER_STATE_PARAMS_NOT_OBJECT, name);
				}
				params = (Map<String, String>) paramsObject;
			}
			else {
				response.addMessage(PARSER_STATE_UNSUPPORTED_PROPERTY, entry.getKey(), name);
			}
		}
		
		
		if (params == null) {
			response.addMessage(PARSER_STATE_PARAMS_MISSING, name);
			params = new HashMap<String,String>();
		}
		else if (params.size() == 0) {
			response.addMessage(PARSER_STATE_PARAMS_EMPTY, name);
		}

		return new StateImpl(name, params);
	}

}
