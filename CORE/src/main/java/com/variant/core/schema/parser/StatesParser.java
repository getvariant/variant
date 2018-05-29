package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.DUPE_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.NAME_INVALID;
import static com.variant.core.schema.parser.error.SemanticError.NAME_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_EMPTY_LIST;
import static com.variant.core.schema.parser.error.SemanticError.PROPERTY_NOT_LIST;
import static com.variant.core.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;

import java.util.List;
import java.util.Map;

import com.variant.core.UserError.Severity;
import com.variant.core.impl.CoreException;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.VariantException;
import com.variant.core.schema.Hook;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.State;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.StateImpl;
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

		Location statesLocation = rootLocation.plusObj(KEYWORD_STATES);
		
		try {

			List<Map<String, ?>> rawStates = (List<Map<String, ?>>) statesObject;
			
			if (rawStates.size() == 0) {
				response.addMessage(statesLocation, PROPERTY_EMPTY_LIST, KEYWORD_STATES);
			}
			
			int index = 0;
			for (Map<String, ?> rawState: rawStates) {

				Location stateLocation = statesLocation.plusIx(index++);
				
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
					response.addMessage(stateLocation, DUPE_OBJECT, state.getName());
				}
				
				// If no errors, register state scoped hooks.
				if (errorCount.intValue() == 0) {
					for (Hook hook: state.getHooks()) hooksService.initHook(hook, response);
	
					// Post the state parsed event.
					try {
						hooksService.post(new StateParsedLifecycleEventImpl(state, response));
					}
					catch (VariantException e) {
						response.addMessage(ServerError.HOOK_UNHANDLED_EXCEPTION, StateParsedLifecycleEventImpl.class.getName(), e.getMessage());
						throw e;
					}
				}
				response.setMessageListener(null);
			}
		}
		catch (ClassCastException e) {
			response.addMessage(rootLocation.plusProp(KEYWORD_STATES), PROPERTY_NOT_LIST, KEYWORD_STATES);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}
	
	/**
	 * Parse a state
	 */
	private static StateImpl parseState(Map<String, ?> rawState, Location stateLocation, final ParserResponse response) {
		
		String name = null;
		boolean nameFound = false;
		
		// Pass 1: figure out the name.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(stateLocation.plusProp(KEYWORD_NAME), NAME_INVALID, KEYWORD_NAME);
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(stateLocation.plusProp(KEYWORD_NAME), NAME_INVALID, KEYWORD_NAME);
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(stateLocation, NAME_MISSING);
			}
			return null;
		}
		
		StateImpl result = new StateImpl(response.getSchema(), name);
		
		// Pass 2: Parse parameters and hooks.
		for(Map.Entry<String, ?> entry: rawState.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) continue;
			
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_PARAMETERS)) {
				Location paramsLocation = stateLocation.plusObj(KEYWORD_PARAMETERS);
				result.setParameterMap(ParamsParser.parse(entry.getValue(), paramsLocation, response));
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
				Location hooksLocation = stateLocation.plusObj(KEYWORD_HOOKS);
				HooksParser.parseStateHooks(entry.getValue(), result, hooksLocation, response);
			}
			else {
				response.addMessage(stateLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}
		

		return result;
	}

}
