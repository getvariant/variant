package com.variant.core.config.parser;

import java.util.List;
import java.util.Map;

import com.variant.core.config.View;
import com.variant.core.error.ErrorTemplate;

/**
 * Parse the VIEWS clause.
 * @author Igor
 *
 */
public class ViewsParser implements Keywords {

	/**
	 * Parse the VIEWS clause.
	 * @param result
	 * @param viewsObject
	 */
	 static void parseViews(Object viewsObject, ParserResponse response) {
		List<Map<String, ?>> rawViews = null;
		try {
			rawViews = (List<Map<String, ?>>) viewsObject;
		}
		catch (Exception e) {
			response.addError(ErrorTemplate.INTERNAL, e.getMessage());
		}
		
		if (rawViews.size() == 0) {
			response.addError(ErrorTemplate.PARSER_NO_VIEWS);
		}
		
		for (Map<String, ?> rawView: rawViews) {
			View view = parseView(rawView, response);
			if (view != null && !((ConfigImpl) response.getConfig()).addView(view)) {
				response.addError(ErrorTemplate.PARSER_VIEW_NAME_DUPE, view.getName());
			}
		}
	}
	
	/**
	 * 
	 * @param view
	 * @param response
	 */
	private static View parseView(Map<String, ?> view, final ParserResponse response) {
		
		String name = null, path = null;
		boolean nameFound = false, pathFound = false;
		
		// Pass 1: figure out the name.
		for(Map.Entry<String, ?> entry: view.entrySet()) {

			if (entry.getKey().equalsIgnoreCase(NAME)) {
				nameFound = true;
				Object nameObject = entry.getValue();
				if (! (nameObject instanceof String)) {
					response.addError(ErrorTemplate.PARSER_VIEW_NAME_NOT_STRING);
				}
				else {
					name = (String) nameObject;
				}
				break;
			}
		}

		if (name == null) {
			if (!nameFound) {
				response.addError(ErrorTemplate.PARSER_VIEW_NAME_MISSING);
			}
			return null;
		}
		
		// Pass 2: Finish parsing if we have the name.
		for(Map.Entry<String, ?> entry: view.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase(NAME)) continue;
			
			if (entry.getKey().equalsIgnoreCase(PATH)) {
				pathFound = true;
				Object pathObject = entry.getValue();
				if (! (pathObject instanceof String)) {
					response.addError(ErrorTemplate.PARSER_VIEW_PATH_NOT_STRING, name);
				}
				path = (String) pathObject;
			}
			else {
				response.addError(ErrorTemplate.PARSER_VIEW_UNSUPPORTED_PROPERTY, entry.getKey(), name);
			}
		}
		
		
		if (path == null) {
			if (!pathFound) {
				response.addError(ErrorTemplate.PARSER_VIEW_PATH_MISSING, name);
			}
			return null;
		}
		
		return new ViewImpl(name, path);
	}

}
