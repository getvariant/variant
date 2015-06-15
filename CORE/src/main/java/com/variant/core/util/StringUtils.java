package com.variant.core.util;

public class StringUtils {

	/**
	 * Does the first string equal (ignore case) to any of the subsequent strings?
	 * @param arg1
	 * @param args
	 * @return
	 */
	public static boolean equalsIgnoreCase(String arg1, String...args) {
		for (String s: args) if (arg1.equalsIgnoreCase(s)) return true;
		return false;
	}
}
