package com.variant.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Igor
 *
 */
public class VariantListUtils {

	/**
	 * Create a new List with the given elements.
	 * @param elems
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> list(T...elems) {
		ArrayList<T> result = new ArrayList<T>(elems.length);
		for (T elem: elems) result.add(elem);
		return result;
	}
}
