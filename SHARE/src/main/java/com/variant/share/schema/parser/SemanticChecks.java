package com.variant.share.schema.parser;

/**
 * 
 * @author Igor Urisman
 *
 */
class SemanticChecks {

	/**
	 * Check that a string is a proper name:
	 * SQL name rule: letter (Unicode), digit and underscore, does not start with number. Case sensitive.
	 * @param string
	 * @return
	 */
	static boolean isName(String string) {
		if (string.length() == 0) return false;
		boolean first = true;
		for (char c: string.toCharArray()) {
			if (first) {
				first = false;
				if (!(Character.isLetter(c) || c == '_')) return false;
			}
			if (!(Character.isLetter(c) || Character.isDigit(c) || c == '_')) return false;
		}
		return true;
	}
}
