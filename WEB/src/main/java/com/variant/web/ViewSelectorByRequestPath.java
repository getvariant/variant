package com.variant.web;

import java.util.Collection;
import java.util.regex.Pattern;

import com.variant.core.ViewSelectorByPath;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.View;

/**
 * 
 * @author Igor
 *
 */
public class ViewSelectorByRequestPath implements ViewSelectorByPath {

	/**
	 * This implements the whole path matching logic.
	 * Package visibility to expose to tests.
	 */
	static boolean match(String pattern, String string) {

		if (!pattern.startsWith("/")) throw new VariantInternalException("Pattern must start with [/] but was [" + pattern + "]");
		if (!string.startsWith("/")) throw new VariantInternalException("String must start with [/] but was [" + string + "]");
		
		// Expand '//', otherwise they may get eaten by the splitter algorithm.
		// Keep looking for '//' until none. This is needed to account for '///'
		String expandedPattern = pattern;
		while (expandedPattern.indexOf("//") >= 0) {
			expandedPattern = expandedPattern.replaceAll("//", "/~.*/");
		}
		
		String[] stringTokens = string.split("/");		
		String patternTokens[] = expandedPattern.split("/");
		
		// Start with 1 because the first token in both will always be an empty string because the
		// first character in both is '/'.
		for (int i = 1; ;i++) {
			if (i == stringTokens.length) {
				if (i == patternTokens.length) {
					// out of input tokens and out of pattern tokens
					return true;
				}
				else {
					// out of input tokens but still have pattern tokens.
					// Ok only if hey all match the empty string.
					for (int j = i; j < patternTokens.length; j++) {
						if (!Pattern.compile(toRegex(patternTokens[j])).matcher("").matches()) return false;
					}
					return true;
				}
			}
			else {
				if (i == patternTokens.length) {
					// still have input tokens but out of pattern tokens
					return false;
				}
				else {
					// have an input token and a pattern token - match them.
					String regex = toRegex(patternTokens[i]);
					if (!Pattern.compile(regex).matcher(stringTokens[i]).matches()) return false;
				}
			}
		}
	}
	
	/**
	 * convert a pattern token to a regex.
	 * @param string
	 * @return
	 */
	private static String toRegex(String token) {
		return token.startsWith("~") ? token.substring(1) : "\\Q" + token + "\\E";
	}
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Return the first matching.
	 */
	@Override
	public View select(String path, Collection<View> views) {
		for (View view: views) {
			if (match(view.getPath(), path)) return view;
		}
		return null;
	}
	
}
