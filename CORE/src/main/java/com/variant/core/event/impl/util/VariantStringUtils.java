package com.variant.core.event.impl.util;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariantStringUtils {
	
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
	
	/**
	 * Generate a random 64 bit binary number as hexadecimal string.
	 * @param rand
	 * @return
	 */
	public static String random64BitString(Random rand) {
		return Long.toHexString(rand.nextLong()).toUpperCase();
	}

	/**
	 * Generate a random 128 bit binary number as hexadecimal string.
	 * @param rand
	 * @return
	 */
	public static String random128BitString(Random rand) {
		return random64BitString(rand) + random64BitString(rand);
	}

	/**
	 * Object[] toString();
	 * @param c
	 * @param separator
	 * @return
	 */
	public static String toString(Object[] objectArray, String separator) {

		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Object o: objectArray) {
			if (first) first = false;
			else result.append(separator);
			result.append(o);
		}
		return result.toString();
	}

	/**
	 * Collection toString();
	 * @param c
	 * @param separator
	 * @return
	 */
	public static String toString(Collection<?> c, String separator) {
		
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Object o: c) {
			if (first) first = false;
			else result.append(separator);
			result.append(o);
		}
		return result.toString();
	}
	/**
	 * Collection toString();
	 * @param c
	 * @param separator
	 * @return
	 */
	public static String toString(Map<?,?> map, String separator) {
		
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Map.Entry<?, ?> e: map.entrySet()) {
			if (first) first = false;
			else result.append(separator);
			result.append(e.getKey()).append("->").append(e.getValue());
		}
		return result.toString();
	}

	/**
	 * Pull a region matching the regex out of the input string.
	 * @param input
	 * @param regex
	 * @return
	 */
	public static String splice(String input, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		return matcher.find() ? matcher.group(0) : null;
	}
	
	/**
	 * Drop the suffix, i.e. the last dot and what follows. Leave as is if no dot.
	 * @param in
	 * @return
	 */
	public static String baseName(String in) {
		int i = in.lastIndexOf('.');
		return i < 0 ? in : in.substring(0, i);
	}
}
