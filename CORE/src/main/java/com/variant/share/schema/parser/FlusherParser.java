package com.variant.share.schema.parser;

import static com.variant.share.schema.parser.error.SemanticError.NAME_INVALID;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_MISSING;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_NOT_OBJECT;
import static com.variant.share.schema.parser.error.SemanticError.UNSUPPORTED_PROPERTY;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.share.error.CoreException;
import com.variant.share.schema.Flusher;
import com.variant.share.schema.impl.SchemaFlusherImpl;
import com.variant.share.schema.parser.error.SemanticError.Location;

/**
 * Hooks parser
 * @author Igor
 */

public class FlusherParser implements Keywords {	

	/**
	 * Parse event flusher
	 * @param rawFlusher.
	 * @param response parser response in progress.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Flusher parse(Object rawFlusher, Location metaLocation, ParserResponse response) {

		String className = null;
		Optional<String> init = Optional.empty();
		Location flusherLocation = metaLocation.plusObj(KEYWORD_FLUSHER);
		
		Map<String, ?> rawMap;
		try {
			rawMap = (Map<String,?>) rawFlusher;
		}
		catch (ClassCastException e) {
			response.addMessage(flusherLocation, PROPERTY_NOT_OBJECT, KEYWORD_FLUSHER);
			return null;
		}
		
		for(Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_CLASS)) {
				Object classNameObject = entry.getValue();
				if (! (classNameObject instanceof String)) {
					response.addMessage(flusherLocation, NAME_INVALID);
					return null;
				}
				else {
					className = (String) classNameObject;
				}
			}
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_INIT)) {
				
				if (entry.getValue() == null) {
					// Explicit null
					init = Optional.of("null");
				}
				else {
					// Non null init. An arbitrary json object. Simply convert it to string
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
			else {
				response.addMessage(flusherLocation.plusProp(entry.getKey()), UNSUPPORTED_PROPERTY, entry.getKey());
			}
		}
	
		if (className == null) {
			response.addMessage(flusherLocation, PROPERTY_MISSING, "class");
			return null;
		}
		else {
			return new SchemaFlusherImpl(className, init);
		}
	}
}
