package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.ParserError.*;

import java.util.List;
import java.util.Map;

import com.variant.core.CoreException;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.Hook;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.impl.UserHookImpl;

/**
 * Parse the META clause.
 * @author Igor
 *
 */
public class MetaParser implements Keywords {
			
	/**
	 * Parse the META clause.
	 * @param statesObject
	 * @param response 
	 */
	@SuppressWarnings("unchecked")
	static void parse(Object metaRaw, ParserResponseImpl response) {

		SchemaImpl schema = (SchemaImpl) response.getSchema();
		
		try {
			
			Map<String,?> metaObject = null;
			try {
				metaObject = (Map<String,?>) metaRaw;
			}
			catch (ClassCastException e) {
				response.addMessage(META_NOT_OBJECT);
				return;
			}

			String name = null, comment = null;
			boolean nameFound = false;
			
			for(Map.Entry<String, ?> entry: metaObject.entrySet()) {
				
				if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
					nameFound = true;
					try {
						name = (String) entry.getValue();
						if (!SemanticChecks.isName(name)) {
							response.addMessage(META_NAME_INVALID);
						}
					}
					catch (ClassCastException e) {
						response.addMessage(META_NAME_INVALID);
					}
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_COMMENT)) {
					try {
						comment = (String) entry.getValue();
					}
					catch (ClassCastException e) {
						response.addMessage(META_COMMENT_INVALID);
					}
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
					try {

						List<?> rawHooks = (List<?>) entry.getValue();
												
						for (Object rawHook: rawHooks) {
							Hook hook = parseHook(rawHook, response);
							
							if (hook != null && !((SchemaImpl) response.getSchema()).addHook(hook)) {
								response.addMessage(META_HOOK_NAME_DUPE, hook.getName());
							}
						}
					}
					catch (ClassCastException e) {
						response.addMessage(META_HOOKS_NOT_LIST);
					}
					catch (Exception e) {
						throw new CoreException.Internal(e);
					}
				}

				else {
					response.addMessage(META_UNSUPPORTED_PROPERTY,  entry.getKey());
				}
			}
			
			if (!nameFound) {
				response.addMessage(META_NAME_MISSING);
			}
			
			if (response.hasMessages(Severity.ERROR)) return;
					
			schema.setMeta(name, comment);
			
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
	private static Hook parseHook(Object rawHook, final ParserResponseImpl response) {
		String name = null;
		String className = null;
		
		// Pass 1: figure out the name.
		boolean nameFound = false;
		
		Map<String, ?> rawMap;
		try {
			rawMap = (Map<String,?>) rawHook;
		}
		catch (ClassCastException e) {
			response.addMessage(META_HOOKS_NOT_OBJECT);
			return null;
		}
		
		for (Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addMessage(META_HOOK_NAME_INVALID);
				}
				else {
					name = (String) nameObject;
					if (!SemanticChecks.isName(name)) {
						response.addMessage(META_HOOK_NAME_INVALID);
					}
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addMessage(META_HOOK_NAME_MISSING);
			}
			return null;
		}

		// Pass 2: figure out the REST.
		for(Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				continue;
			} 
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_CLASS_NAME)) {
				Object classNameObject = entry.getValue();
				if (! (classNameObject instanceof String)) {
					response.addMessage(META_CLASS_NAME_INVALID);
				}
				else {
					className = (String) classNameObject;
				}
			}
			else {
				response.addMessage(META_HOOK_UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}
	
		if (className == null) {
			response.addMessage(META_HOOK_CLASS_NAME_MISSING, name);
			return null;
		}
		else {
			return new UserHookImpl(name, className);
		}
	}
}
