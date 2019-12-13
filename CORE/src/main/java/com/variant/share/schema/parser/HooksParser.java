package com.variant.share.schema.parser;

import static com.variant.share.schema.parser.error.SemanticError.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.share.error.CoreException;
import com.variant.share.schema.Hook;
import com.variant.share.schema.MetaScopedHook;
import com.variant.share.schema.StateScopedHook;
import com.variant.share.schema.VariationScopedHook;
import com.variant.share.schema.impl.MetaImpl;
import com.variant.share.schema.impl.SchemaHookImpl;
import com.variant.share.schema.impl.StateImpl;
import com.variant.share.schema.impl.StateScopedHookImpl;
import com.variant.share.schema.impl.VariationImpl;
import com.variant.share.schema.impl.VariationScopedHookImpl;
import com.variant.share.schema.parser.error.SemanticError.Location;
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
	static void parseMetaHooks(Object hooksObject, MetaImpl meta, Location metaLocation, ParserResponse response) {		
		
		Location hooksLocation = metaLocation.plusObj(KEYWORD_HOOKS);
		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
			ArrayList<MetaScopedHook> hooks = new ArrayList<MetaScopedHook>(rawHooks.size());
			
			int i = 0;
			for (Object rawHook: rawHooks) {
				
				Location hookLocation = hooksLocation.plusIx(i++);
				MetaScopedHook hook = parseHook(rawHook, hookLocation, response);
				if (hook != null) hooks.add(hook); 
			}
			meta.SetHooks(hooks);
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
	private static MetaScopedHook parseHook(Object rawHook, Location hookLocation, ParserResponse response) {
		
		String className = null;
		Optional<String> init = Optional.empty();
				
		Map<String, ?> rawMap;
		try {
			rawMap = (Map<String,?>) rawHook;
		}
		catch (ClassCastException e) {
			response.addMessage(hookLocation, ELEMENT_NOT_OBJECT, KEYWORD_HOOKS);
			return null;
		}
		
		for(Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_CLASS)) {
				Object classNameObject = entry.getValue();
				if (! (classNameObject instanceof String)) {
					response.addMessage(hookLocation.plusProp(KEYWORD_CLASS), PROPERTY_NOT_STRING);
				}
				else {
					className = (String) classNameObject;
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_INIT)) {
				Object initObject = entry.getValue();
				if (initObject == null) {
					// Explicit null
					init = Optional.of("null");
				}
				else {
					// Non-null
					if (! (initObject instanceof Map)) {
						response.addMessage(hookLocation.plusProp(KEYWORD_INIT), PROPERTY_NOT_OBJECT, "init");
					}
					else {
						// Init is an arbitrary json object. Simply convert it to string
						// and let server repackage it as typesafe config.
						ObjectMapper mapper = new ObjectMapper();
						mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
						try {
							init = Optional.of(mapper.writeValueAsString(entry.getValue()));
						}
						catch (Exception e) {
							throw new CoreException.Internal("Unable to re-serialize hook init [" + entry.getValue().toString() + "]", e);
						}
					}
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
			return new SchemaHookImpl(className, init, hookLocation);
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
			ArrayList<StateScopedHook> hooks = new ArrayList<StateScopedHook>(rawHooks.size());

			int index = 0;
			for (Object rawHook: rawHooks) {
				
				Location hookLocation = stateLocation.plusIx(index++);
				
				MetaScopedHook hook = parseHook(rawHook, hookLocation, response);
				
				if (hook != null) {
					// The method above created a schema level hook, but in this case we need a test
					// domian hook.
					hooks.add(new StateScopedHookImpl(hook.getClassName(), hook.getInit(), hookLocation, state));
				}	
			}
			state.setHooks(hooks);
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
	static void parseVariationHook(Object hooksObject, VariationImpl test, Location testLocation, ParserResponse response) {		
		try {
			List<?> rawHooks = (List<?>) hooksObject;
			ArrayList<VariationScopedHook> hooks = new ArrayList<VariationScopedHook>(rawHooks.size());
						
			int index = 0;
			for (Object rawHook: rawHooks) {
				
				Location hookLocation = testLocation.plusIx(index++);
				
				Hook hook = parseHook(rawHook, hookLocation, response);
				
				if (hook != null) {
					// The method above created a schema level hook, but in this case we need a test
					// domian hook.
					hooks.add(new VariationScopedHookImpl(hook.getClassName(), hook.getInit(), hookLocation, test));
				}	
			}
			test.setHooks(hooks);
		}
		catch (ClassCastException e) {
			response.addMessage(testLocation, PROPERTY_NOT_LIST, "hooks");
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
	}

}
