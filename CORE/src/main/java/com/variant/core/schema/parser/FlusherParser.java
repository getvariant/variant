package com.variant.core.schema.parser;

import static com.variant.core.schema.parser.error.SemanticError.*;

import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.CoreException;
import com.variant.core.schema.Flusher;
import com.variant.core.schema.impl.SchemaFlusherImpl;
import com.variant.core.schema.parser.error.SemanticError.Location;

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
		String init = null;
		Location flusherLocation = metaLocation.plus(KEYWORD_FLUSHER);
		
		Map<String, ?> rawMap;
		try {
			rawMap = (Map<String,?>) rawFlusher;
		}
		catch (ClassCastException e) {
			response.addMessage(flusherLocation, PROPERTY_NOT_OBJECT, KEYWORD_FLUSHER);
			return null;
		}
		
		for(Map.Entry<String, ?> entry: rawMap.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(KEYWORD_NAME)) {
				continue;
			} 
			else if (entry.getKey().equalsIgnoreCase(KEYWORD_CLASS)) {
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
				response.addMessage(flusherLocation, UNSUPPORTED_PROPERTY, entry.getKey());
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
