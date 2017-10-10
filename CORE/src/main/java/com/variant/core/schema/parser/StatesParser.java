package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.STATES_CLAUSE_EMPTY;
import static com.variant.core.schema.parser.error.SemanticError.STATES_CLAUSE_NOT_LIST;
import static com.variant.core.schema.parser.error.SemanticError.STATE_NAME_DUPE;
import static com.variant.core.schema.parser.error.SemanticError.STATE_NAME_INVALID;
import static com.variant.core.schema.parser.error.SemanticError.STATE_NAME_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.STATE_UNSUPPORTED_PROPERTY;

import java.util.List;
import java.util.Map;

import com.variant.core.CoreException;
import com.variant.core.UserError.Severity;
import com.variant.core.VariantException;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.parser.error.CollateralMessage;
import com.variant.core.schema.parser.error.SemanticError.Location;
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
	static void parse(Object statesObject, Location rootLocation, ParserResponse response, HooksService hooksService) {

		Location statesLocation = rootLocation.plus(KEYWORD_STATES);
		
		try {

			List<Map<String, ?>> rawStates = (List<Map<String, ?>>) statesObject;
			
			if (rawStates.size() == 0) {
				response.addMessage(STATES_CLAUSE_EMPTY, statesLocation);
			}
			
			int index = 0;
			for (Map<String, ?> rawState: rawStates) {

				Location stateLocation = statesLocation.plus(index++);
				
				// Increment a local integer count whenever a parse error occurs.
				final MutableInteger errorCount = new MutableInteger(0);
				response.setMessageListener(
						new ParserResponse.MessageListener() {
							@Override
							public void messageAdded(ParserMessage message) {
								if (message.getSeverity().greaterOrEqual(Severity.ERROR)) 
									errorCount.add(1);
							}
				});
				
				// Parse individual state
				State state = parseState(rawState, stateLocation, response);
				if (state != null && !((SchemaImpl) response.getSchema()).addState(state)) {
					response.addMessage(STATE_NAME_DUPE, stateLocation, state.getName());
				}
				
				// If no errors, register state scoped hooks.
				if (errorCount.intValue() == 0) {
					for (Hook hook: state.getHooks()) hooksService.initHook(hook, response);
	
					// Post the state parsed event.
					try {
						hooksService.post(new StateParsedLifecycleEventImpl(state, response));
					}
					catch (VariantException e) {
						response.addMessage(CollateralMessage.HOOK_UNHANDLED_EXCEPTION, StateParsedLifecycleEventImpl.class.getName(), e.getMessage());
						throw e;
					}
				}
				response.setMessageListener(null);
			}
		}
		catch (ClassCastException e) {
			response.addMessage(STATES_CLAUSE_NOT_LIST, rootLocation);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}
	
	/**
	 * Parse a state
	 */
	@SuppressWarnings("unchecked")
	private static StateImpl parseState(Map<String, ?> rawState, Location stateLocation, final ParserResponse response) {
		
		String name = null;
		boolean nameFound = false;
		
		// Pass 1: figure out the name.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(STATE_NAME_INVALID, stateLocation.plus(KEYWORD_NAME));
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(STATE_NAME_INVALID, stateLocation.plus(KEYWORD_NAME));
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(STATE_NAME_MISSING, stateLocation);
			}
			return null;
		}
		
		StateImpl result = new StateImpl(response.getSchema(), name);
		
		// Pass 2: Parse parameters and hooks.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				Location paramsLocation = stateLocation.plus(KEYWORD_PARAMETERS);
				result.setParameterMap(ParamsParser.parse(entry.getValue(), paramsLocation, response));
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
				Location hooksLocation = stateLocation.plus(KEYWORD_HOOKS);
				HooksParser.parseStateHooks(entry.getValue(), result, hooksLocation, response);
			}
			else {
				response.addMessage(STATE_UNSUPPORTED_PROPERTY, stateLocation, entry.getKey(), name);
			}
		}
		

		return result;
	}

}
