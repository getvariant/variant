package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.*;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.CoreException;
import com.variant.core.schema.Hook;
import com.variant.core.schema.impl.SchemaHookImpl;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.StateHookImpl;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.schema.impl.TestHookImpl;
import com.variant.core.schema.impl.TestImpl;
import com.variant.core.schema.parser.error.SemanticError.Location;
/**
 * Hooks parser
 * @author Igor
 */

public class HooksParser implements Keywords {

	/**
	 * Parse hooks list with the schema scope. 
	 * Schema is available on the response object. 
	 * @param hooksObject
	 * @param response
	 */
	static void parseMetaHooks(Object hooksObject, Location metaLocation, ParserResponse response) {		
		
		Location hooksLocation = metaLocation.plusObj(KEYWORD_HOOKS);
		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
									
			int i = 0;
			for (Object rawHook: rawHooks) {
				
				Location hookLocation = hooksLocation.plusIx(i++);
				Hook hook = parseHook(rawHook, hookLocation, response);
				
				if (hook != null && !((SchemaImpl) response.getSchema()).addHook(hook)) {
					response.addMessage(hookLocation, DUPE_OBJECT, hook.getName());
				}
			}
		}
		catch (ClassCastException e) {
			response.addMessage(metaLocation.plusObj(KEYWORD_HOOKS), PROPERTY_NOT_LIST, "hooks");
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}
	
	/**
	 * Parse an individual life-cycle hook definition
	 * @param rawHook
	 * @param response
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static Hook parseHook(Object rawHook, Location hookLocation, ParserResponse response) {
		
		String name = null;
		String className = null;
		String init = null;
		
		// Pass 1: figure out the name.
		boolean nameFound = false;
		
		Map<String, ?> rawMap;
		try {
			rawMap = (Map<String,?>) rawHook;
		}
		catch (ClassCastException e) {
			response.addMessage(hookLocation, ELEMENT_NOT_OBJECT, KEYWORD_HOOKS);
			return null;
		}
		
		for (Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(hookLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage( hookLocation.plusProp(KEYWORD_NAME), NAME_INVALID);
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(hookLocation, NAME_MISSING);
			}
			return null;
		}

		// Pass 2: figure out the rest.
		for(Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				continue;
			} 
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_CLASS)) {
				Object classNameObject = entry.getValue();
				if (! (classNameObject instanceof String)) {
					response.addMessage(hookLocation.plusProp(KEYWORD_CLASS), NAME_INVALID);
				}
				else {
					className = (String) classNameObject;
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_INIT)) {
				// Init is an arbitrary json object. Simply convert it to string
				// and let server repackage it as typesafe config.
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

				try {
					init = mapper.writeValueAsString(entry.getValue());
					//String jsonToReparse = "{'init':" + init + "}";
					//mapper.readValue(jsonToReparse, Map.class); // attempt to re-parse.
				}
	 			catch (Exception e) {
	 				throw new CoreException.Internal("Unable to re-serialize hook init [" + entry.getValue().toString() + "]", e);
				}
			}
			else {
				response.addMessage(hookLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}
	
		if (className == null) {
			response.addMessage(hookLocation, PROPERTY_MISSING, "class");
			return null;
		}
		else {
			return new SchemaHookImpl(name, className, init);
		}
	}
	
	/**
	 * Parse hooks list with the state scope. 
	 * @param hooksObject
	 * @param response
	 */
	static void parseStateHooks(Object hooksObject, StateImpl state, Location stateLocation, ParserResponse response) {		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
									
			int index = 0;
			for (Object rawHook: rawHooks) {
				
				Location hookLocation = stateLocation.plusIx(index++);
				
				Hook hook = parseHook(rawHook, hookLocation, response);
				
				if (hook != null) {
					// The method above created a schema level hook, but in this case we need a test
					// domian hook.
					hook = new StateHookImpl(hook.getName(), hook.getClassName(), hook.getInit(), state);
					if (!state.addHook(hook)) {
						response.addMessage(hookLocation, DUPE_OBJECT, hook.getName());
					}
				}	
			}
		}
		catch (ClassCastException e) {
			response.addMessage(stateLocation, PROPERTY_NOT_LIST, "hooks");
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}

	/**
	 * Parse hooks list with the test scope. 
	 * @param hooksObject
	 * @param response
	 */
	static void parseTestHook(Object hooksObject, TestImpl test, Location testLocation, ParserResponse response) {		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
						
			int index = 0;
			for (Object rawHook: rawHooks) {
				
				Location hookLocation = testLocation.plusIx(index++);
				
				Hook hook = parseHook(rawHook, hookLocation, response);
				
				if (hook != null) {
					// The method above created a schema level hook, but in this case we need a test
					// domian hook.
					hook = new TestHookImpl(hook.getName(), hook.getClassName(), hook.getInit(), test);
					if (!test.addHook(hook)) {
						response.addMessage(hookLocation, DUPE_OBJECT, hook.getName());
					}
				}	
			}
		}
		catch (ClassCastException e) {
			response.addMessage(testLocation, PROPERTY_NOT_LIST, "hooks");
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}

}
