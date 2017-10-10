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
		
		Location hooksLocation = metaLocation.plus("/hooks");
		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
									
			int i = 0;
			for (Object rawHook: rawHooks) {
				
				Hook hook = parseHook(rawHook, hooksLocation.plus(i++), response);
				
				if (hook != null && !((SchemaImpl) response.getSchema()).addHook(hook)) {
					response.addMessage(HOOK_NAME_DUPE, hooksLocation, hook.getName());
				}
			}
		}
		catch (ClassCastException e) {
			response.addMessage(HOOKS_NOT_LIST, metaLocation);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}
	
	/**
	 * Parse an individual user hook definition
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
			response.addMessage(HOOK_NOT_OBJECT, hookLocation);
			return null;
		}
		
		for (Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(HOOK_NAME_INVALID, hookLocation.plus("/name"));
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(HOOK_NAME_INVALID, hookLocation.plus("/name"));
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(HOOK_NAME_MISSING, hookLocation);
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
					response.addMessage(HOOK_CLASS_NAME_INVALID, hookLocation.plus("/class"), name);
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
				response.addMessage(HOOK_UNSUPPORTED_PROPERTY, hookLocation, entry.getKey(), name);
			}
		}
	
		if (className == null) {
			response.addMessage(HOOK_CLASS_NAME_MISSING, hookLocation, name);
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
				
				Location hookLocation = stateLocation.plus(index++);
				
				Hook hook = parseHook(rawHook, hookLocation, response);
				
				if (hook != null) {
					// The method above created a schema level hook, but in this case we need a test
					// domian hook.
					hook = new StateHookImpl(hook.getName(), hook.getClassName(), hook.getInit(), state);
					if (!state.addHook(hook)) {
						response.addMessage(HOOK_NAME_DUPE, hookLocation, hook.getName());
					}
				}	
			}
		}
		catch (ClassCastException e) {
			response.addMessage(HOOKS_NOT_LIST, stateLocation);
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
				
				Location hookLocation = testLocation.plus(index++);
				
				Hook hook = parseHook(rawHook, hookLocation, response);
				
				if (hook != null) {
					// The method above created a schema level hook, but in this case we need a test
					// domian hook.
					hook = new TestHookImpl(hook.getName(), hook.getClassName(), hook.getInit(), test);
					if (!test.addHook(hook)) {
						response.addMessage(HOOK_NAME_DUPE, hookLocation, hook.getName());
					}
				}	
			}
		}
		catch (ClassCastException e) {
			response.addMessage(HOOKS_NOT_LIST, testLocation);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}

}
