package com.variant.core.util;

import java.lang.reflect.Array;

public class VariantArrayUtils {

	/**
	 * Append an element to an array
	 * @param a
	 * @param e
	 * @return
	 */
	public static <T> T[] concat(T[] a, T e, Class<T> type) {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(type, a.length + 1);
		for (int i = 0; i < a.length; i++) result[i] = a[i];
		result[a.length] = e;
		return result;
	}

	/**
	 * Prepend an element to an array
	 * @param a
	 * @param e
	 * @return
	 */
	public static <T> T[] concat(T e, T[] a, Class<T> type) {
		@SuppressWarnings("unchecked")
		T[] result = (T[]) Array.newInstance(type, a.length + 1);
		result[0] = e;
		for (int i = 0; i < a.length; i++) result[i+1] = a[i];
		return result;
	}

}
