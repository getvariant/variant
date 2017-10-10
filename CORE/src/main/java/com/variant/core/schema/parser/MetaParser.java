package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.META_COMMENT_INVALID;
import static com.variant.core.schema.parser.error.SemanticError.META_NAME_INVALID;
import static com.variant.core.schema.parser.error.SemanticError.META_NAME_MISSING;
import static com.variant.core.schema.parser.error.SemanticError.META_NOT_OBJECT;
import static com.variant.core.schema.parser.error.SemanticError.META_UNSUPPORTED_PROPERTY;

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
	static void parse(Object metaRaw, Location rootLocation, ParserResponse response) {

		SchemaImpl schema = (SchemaImpl) response.getSchema();
		Location metaLocation = rootLocation.plus("meta");
		
		try {
			
			Map<String,?> metaObject = null;
			try {
				metaObject = (Map<String,?>) metaRaw;
			}
			catch (ClassCastException e) {
				response.addMessage(META_NOT_OBJECT, rootLocation);
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
							response.addMessage(META_NAME_INVALID, metaLocation.plus("/name"));
						}
					}
					catch (ClassCastException e) {
						response.addMessage(META_NAME_INVALID, metaLocation.plus("/name"));
					}
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_COMMENT)) {
					try {
						comment = (String) entry.getValue();
					}
					catch (ClassCastException e) {
						response.addMessage(META_COMMENT_INVALID, metaLocation.plus("/comment"));
					}
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_HOOKS)) {
					HooksParser.parseMetaHooks(entry.getValue(), metaLocation, response);
				}
				else if (entry.getKey().equalsIgnoreCase(KEYWORD_FLUSHER)) {
					flusher = FlusherParser.parse(entry.getValue(), metaLocation, response);
				}

				else {
					response.addMessage(META_UNSUPPORTED_PROPERTY,  metaLocation, entry.getKey());
				}
			}
			
			if (!nameFound) {
				response.addMessage(META_NAME_MISSING, metaLocation);
			}
			
			if (response.hasMessages(Severity.ERROR)) return;
					
			schema.setMeta(name, comment, flusher);
			
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
	}
}
