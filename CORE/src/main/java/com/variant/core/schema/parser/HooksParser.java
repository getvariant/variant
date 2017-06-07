package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.ParserError.*;

import java.util.List;
import java.util.Map;

import com.variant.core.CoreException;
import com.variant.core.LifecycleEvent.Domain;
import com.variant.core.schema.Hook;
import com.variant.core.schema.impl.SchemaHookImpl;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.TestHookImpl;
import com.variant.core.schema.impl.TestImpl;

/**
 * Hooks parser
 * @author Igor
 */

public class HooksParser implements Keywords {

	/**
	 * Parse hooks list with the schema domain. 
	 * Schema is available on the response object. 
	 * @param hooksObject
	 * @param response
	 */
	static void parse(Object hooksObject, ParserResponseImpl response) {		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
									
			for (Object rawHook: rawHooks) {
				Hook hook = parseHook(rawHook, Domain.SCHEMA, response);
				
				if (hook != null && !((SchemaImpl) response.getSchema()).addHook(hook)) {
					response.addMessage(HOOK_NAME_DUPE, hook.getName());
				}
			}
		}
		catch (ClassCastException e) {
			response.addMessage(HOOKS_NOT_LIST);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}
	
	/**
	 * Parse hooks list with the test domain. 
	 * @param hooksObject
	 * @param response
	 */
	static void parse(Object hooksObject, TestImpl test, ParserResponseImpl response) {		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
									
			for (Object rawHook: rawHooks) {
				
				Hook hook = parseHook(rawHook, Domain.SCHEMA, response);
				
				if (hook != null) {
					// The method above created a schema level hook, but in this case we need a test
					// domian hook.
					hook = new TestHookImpl(hook.getName(), hook.getClassName(), hook.getInit(), test);
					if (!test.addHook(hook)) {
						response.addMessage(HOOK_NAME_DUPE, hook.getName());
					}
				}	
			}
		}
		catch (ClassCastException e) {
			response.addMessage(HOOKS_NOT_LIST);
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
	private static Hook parseHook(Object rawHook, Domain domain, ParserResponseImpl response) {
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
			response.addMessage(HOOKS_NOT_OBJECT);
			return null;
		}
		
		for (Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(HOOK_NAME_INVALID);
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(HOOK_NAME_INVALID);
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(HOOK_NAME_MISSING);
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
					response.addMessage(HOOK_CLASS_NAME_INVALID, name);
				}
				else {
					className = (String) classNameObject;
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_INIT)) {
				Object initObject = entry.getValue();
				if (! (initObject instanceof String)) {
					response.addMessage(HOOK_INIT_INVALID, name);
				}
				else {
					init = (String) initObject;
				}
			}
			else {
				response.addMessage(HOOK_UNSUPPORTED_PROPERTY, entry.getKey(), name);
			}
		}
	
		if (className == null) {
			response.addMessage(HOOK_CLASS_NAME_MISSING, name);
			return null;
		}
		else {
			return new SchemaHookImpl(name, className, init);
		}
	}
}
