package com.variant.share.schema.parser;

import static com.variant.share.schema.parser.error.SemanticError.PARAM_NAME_DUPE;
import static com.variant.share.schema.parser.error.SemanticError.PARAM_NAME_INVALID;
import static com.variant.share.schema.parser.error.SemanticError.PARAM_VALUE_INVALID;
import static com.variant.share.schema.parser.error.SemanticError.PROPERTY_NOT_OBJECT;

import java.util.Map;

import com.variant.share.error.CoreException;
import com.variant.share.schema.parser.error.SemanticError.Location;
import com.variant.share.util.CaseInsensitiveLinkedMap;

/**
 * State Parameters parser
 * @author Igor
 */

public class ParamsParser implements Keywords {
	
	/**
	 * Parse parameter list. 
	 */
	static Map<String, String> parse(Object paramsObject, Location paramsLocation, ParserResponse response) {		
		
		CaseInsensitiveLinkedMap<String> result = new CaseInsensitiveLinkedMap<String>();
		
		try {
			@SuppressWarnings("unchecked")
         Map<String, ?> rawParams = (Map<String, ?>) paramsObject;
									
			for (Map.Entry<String, ?> entry: rawParams.entrySet()) {
				
			   String name = entry.getKey();
			   
	         if (!SemanticChecks.isName(name)) {
	            response.addMessage(paramsLocation, PARAM_NAME_INVALID, name);
	         }
	         else {
	            Object valueObject = entry.getValue();
	            if (valueObject == null || ! (valueObject instanceof String)) {
	               response.addMessage(paramsLocation.plusProp(name), PARAM_VALUE_INVALID, name);
	            }
	            else {
                  String value = (String) valueObject;
                  if (result.put(name, value) != null) {
                     response.addMessage(paramsLocation.plusProp(name), PARAM_NAME_DUPE, name);
                  }
	            }
	         }

			}
		}
		catch (ClassCastException e) {
			response.addMessage(paramsLocation, PROPERTY_NOT_OBJECT, KEYWORD_PARAMETERS);
		}
		catch (Exception e) {
			throw new CoreException.Internal(e);
		}
		
		return result;
	}
}
