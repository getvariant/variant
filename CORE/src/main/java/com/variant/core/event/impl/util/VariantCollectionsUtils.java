package com.variant.core.event.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.variant.core.util.Tuples;
import com.variant.core.util.Tuples.Pair;

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
	 * Create a new List as a concatenation of given lists.
	 * @param elems
	 * @return
	 */
	@SafeVarargs
	public static <T> List<T> list(List<T>...lists) {
		ArrayList<T> result = new ArrayList<T>();
		for (List<T> list: lists) result.addAll(list);
		return result;
	}

	/**
	 * Create a new set with the given elements.
	 * @param elems
	 * @return
	 */
	@SafeVarargs
	public static <T> Set<T> set(T...elems) {
		HashSet<T> result = new HashSet<T>();
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
	public static boolean isDisjoint(Collection<?> a, Collection<?> b) {
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
	
	/**
	 * Convert a map to a collection of Pairs whose first elem is the key and the second -
	 * the corresponding value.
	 * 
	 * @param map
	 * @return
	 */
	public static <K,V> Collection<Pair<K,V>> mapToPairs(Map<K,V> map) {
		LinkedList<Pair<K,V>> result = new LinkedList<Pair<K,V>>();
		for (Map.Entry<K, V> e: map.entrySet()) {
			result.add(new Pair<K,V>(e.getKey(), e.getValue()));
		}
		return result;
	}
	
}
