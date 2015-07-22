package com.variant.core.util;

import java.util.ArrayList;

/**
 * 
 * @author Igor
 *
 */
public class VariantListUtils {

	/**
	 * Create a new ArrayList with the given elems.
	 * @param elems
	 * @return
	 */
	@SafeVarargs
	public static <T> ArrayList<T> newArrayList(T...elems) {
		ArrayList<T> result = new ArrayList<T>(elems.length);
		for (T elem: elems) result.add(elem);
		return result;
	}
}
