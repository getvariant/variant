package com.variant.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Igor
 *
 */
public class VariantCollectionsUtils {

	public static final List<?> EMPTY_LIST = list();
	
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
	
	/**
	 * Does collection a contain collection b?
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean contains(Collection<?> a, Collection<?> b) {
		for (Object fromA: a) if (!b.contains(fromA)) return false;
		return true;
	}
	
	/**
	 * Are two collections disjoint, i.e. have no elements in common?
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean isDisjoint(Collection<?> a, Collection<?> b) {
		Collection<?> small, big;
		if (a.size() < b.size()) { small = a; big = b;}
		else { small = b; big = a;}
		for (Object fromSmall: small) if (big.contains(fromSmall)) return false;
		return true;
	}
	
	/**
	 * Merge maps left to right.
	 * @param maps
	 * @return a new map containing all keys from all maps and each key's value comes from the last
	 *         map where it is defined if scanned left to right.
	 */
	public static Map<?,?> mapMerge(Map<?,?>...maps) {
		Map<Object,Object> result = new HashMap<Object,Object>();
		for (int i = 0; i < maps.length; i++) {
			result.putAll(maps[i]);
		}
		return result;
	}
}
