package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.*;

import java.util.Map;

import com.variant.core.CoreException;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.impl.SchemaImpl;
import com.variant.core.schema.parser.error.SemanticError.Location;

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
	static void parse(Object metaRaw, Location metaLocation, ParserResponse response) {

		SchemaImpl schema = (SchemaImpl) response.getSchema();
		
		try {
			
			Map<String,?> metaObject = null;
			try {
				metaObject = (Map<String,?>) metaRaw;
			}
			catch (ClassCastException e) {
				response.addMessage(metaLocation, PROPERTY_NOT_OBJECT, "meta");
				return;
			}

			String name = null, comment = null;
			boolean nameFound = false;
            Flusher flusher = null;
            
			for(Map.Entry<String, ?> entry: metaObject.entrySet()) {
				
				if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
					nameFound = true;
					try {
						name = (String) entry.getValue();
						if (!SemanticChecks.isName(name)) {
							response.addMessage(metaLocation.plus("/name"), NAME_INVALID);
						}
					}
					catch (ClassCastException e) {
						response.addMessage(metaLocation.plus("/name"), NAME_INVALID);
					}
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_COMMENT)) {
					try {
						comment = (String) entry.getValue();
					}
					catch (ClassCastException e) {
						response.addMessage(metaLocation.plus("/comment"), PROPERTY_NOT_STRING, "comment");
					}
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
					HooksParser.parseMetaHooks(entry.getValue(), metaLocation, response);
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_FLUSHER)) {
					flusher = FlusherParser.parse(entry.getValue(), metaLocation, response);
				}

				else {
					response.addMessage(metaLocation, UNSUPPORTED_PROPERTY, entry.getKey());
				}
			}
			
			if (!nameFound) {
				response.addMessage(metaLocation, NAME_MISSING);
			}
			
			if (response.hasMessages(Severity.ERROR)) return;
					
			schema.setMeta(name, comment, flusher);
			
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
	}
}
