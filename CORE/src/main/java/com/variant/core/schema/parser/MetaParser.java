package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.ParserError.*;

import java.util.Map;

import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.schema.impl.SchemaImpl;

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
			throw new RuntimeInternalException(e);
		}
		
	}

}