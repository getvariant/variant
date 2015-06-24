package com.variant.core.util;

import java.util.Random;

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
}
