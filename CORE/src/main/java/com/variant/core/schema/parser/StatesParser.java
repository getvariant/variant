package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.ParserError.NO_STATES;
import static com.variant.core.schema.parser.ParserError.STATES_CLAUSE_NOT_LIST;
import static com.variant.core.schema.parser.ParserError.STATE_NAME_DUPE;
import static com.variant.core.schema.parser.ParserError.STATE_NAME_INVALID;
import static com.variant.core.schema.parser.ParserError.STATE_NAME_MISSING;
import static com.variant.core.schema.parser.ParserError.STATE_PARAMS_NOT_OBJECT;
import static com.variant.core.schema.parser.ParserError.STATE_UNSUPPORTED_PROPERTY;

import java.util.List;
import java.util.Map;

import com.variant.core.CommonError;
import com.variant.core.CoreException;
import com.variant.core.UserError.Severity;
import com.variant.core.VariantException;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.util.MutableInteger;

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
	static void parse(Object statesObject, ParserResponseImpl response, HooksService hooksService) {

		try {

			List<Map<String, ?>> rawStates = (List<Map<String, ?>>) statesObject;
			
			if (rawStates.size() == 0) {
				response.addMessage(NO_STATES);
			}
			
			for (Map<String, ?> rawState: rawStates) {

				// Increment a local integer count whenever a parse error occurs.
				final MutableInteger errorCount = new MutableInteger(0);
				response.setParserListener(
						new ParserListener() {
							@Override
							public void messageAdded(ParserMessage message) {
								if (message.getSeverity().greaterOrEqual(Severity.ERROR)) 
									errorCount.add(1);
							}
				});
				
				// Parse individual state
				State state = parseState(rawState, response);
				if (state != null && !((SchemaImpl) response.getSchema()).addState(state)) {
					response.addMessage(STATE_NAME_DUPE, state.getName());
				}
				
				// If no errors, register state scoped hooks.
				if (errorCount.intValue() == 0) {
					for (Hook hook: state.getHooks()) hooksService.initHook(hook, response);
	
					// Post the state parsed event.
					try {
						hooksService.post(new StateParsedLifecycleEventImpl(state, response));
					}
					catch (VariantException e) {
						response.addMessage(CommonError.HOOK_UNHANDLED_EXCEPTION, StateParsedLifecycleEventImpl.class.getName(), e.getMessage());
						throw e;
					}
				}
				response.setParserListener(null);
			}
		}
		catch (ClassCastException e) {
			response.addMessage(STATES_CLAUSE_NOT_LIST);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}
	
	/**
	 * Parse a state
	 */
	@SuppressWarnings("unchecked")
	private static StateImpl parseState(Map<String, ?> rawState, final ParserResponseImpl response) {
		
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
		
		StateImpl result = new StateImpl(response.getSchema(), name);
		
		// Pass 2: Parse parameters and hooks.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				Object paramsObject = entry.getValue();
				if (! (paramsObject instanceof Map)) {
					response.addMessage(STATE_PARAMS_NOT_OBJECT, name);
				}
				params = (Map<String, String>) paramsObject;
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
				HooksParser.parse(entry.getValue(), result, response);
			}
			else {
				response.addMessage(STATE_UNSUPPORTED_PROPERTY, entry.getKey(), name);
			}
		}
		
		if (params != null) result.setParameterMap(params);

		return result;
	}

}
