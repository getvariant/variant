package com.variant.core.util;

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
	 * Clone of apache lang3 StringUtils.repeat
	 * @param str
	 * @param repeat
	 * @return
	 */
	public static String repeat(String str, int repeat) {
		if (repeat < 0) throw new IllegalArgumentException("repeat cannot be negative");
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < repeat; i++) result.append(str);
		return result.toString();
	}
	
	/**
	 * Clone of apache lang3 StringUtils.join
	 * @param iterable
	 * @param separator
	 * @return
	 */
	public static String join(Iterable<?> iterable, String separator) {
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Object next: iterable) {
			if (first) first = false;
			else result.append(separator);
			result.append(next);
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
	 * String digest is good for comparing strings that are the same except fragments are rearranged.
	 * 
	 */
	public static long digest(String s) {
		char[] arr = s.toCharArray();
		long result = 0;
		for (char l = '!'; l <= '~'; l++) {
			int cnt = 0;
			for (char c: arr) if (l == c) cnt++;
			result += Character.getNumericValue(l) * cnt;
		}
		return result;
	}
}
