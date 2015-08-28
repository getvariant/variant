package com.variant.web;

import java.util.Collection;
import java.util.regex.Pattern;

import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.View;
import com.variant.core.schema.ViewSelectorByPath;

/**
 * 
 * @author Igor
 *
 */
public class ViewSelectorByRequestPath implements ViewSelectorByPath {

	/**
	 * Package visibility to expose to tests.
	 */
	static boolean match(String pattern, String string) {

		String expandedPath = pattern;
		if (!expandedPath.endsWith("/")) expandedPath += "/";
		// Keep looking for '//' until none. This is needed to account for '///'
		while (expandedPath.indexOf("//") >= 0) {
			expandedPath = expandedPath.replaceAll("//", "/~.*/");
		}
		
		String tokens[] = expandedPath.split("/");
		StringBuilder patternString = new StringBuilder();
		for (String token: tokens) {
			if (token.length() == 0) continue; // first token will be null string.
			patternString.append("/");
			if (token.charAt(0) == '~') patternString.append(token.substring(1));
			else patternString.append(escape(token));
		}
		patternString.append("/");
		Pattern p = Pattern.compile(patternString.toString());
		return p.matcher(string).matches();
	}
	
	/**
	 * Escape all characters with \.
	 * @param string
	 * @return
	 */
	private static String escape(String string) {
		return "\\Q" + string + "\\E";
	}
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//

	@Override
	public View select(String path, Collection<View> views) {
		for (View view: views) {
			if (match(view.getPath(), path)) return view;
		}
		return null;
	}
	
}
