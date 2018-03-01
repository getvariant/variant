package com.variant.core.util;

import java.util.*;

import static com.variant.core.util.Tuples.*;



/**
 * 
 * @author Igor
 *
 */
public class CollectionsUtils {

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
	 * Merge maps left to right into the leftmost map.
	 * @param maps
	 * @return a new map containing all keys from all maps and each key's value comes from the last
	 *         map where it is defined if scanned left to right.
	 */
	@SafeVarargs
	public static void mapMerge(Map<String,String>...maps) {
		for (int i = 1; i < maps.length; i++) {
			maps[0].putAll(maps[i]);
		}
	}
	
	/**
	 * Convert a map to a collection of Pairs whose first elem is the key and the second -
	 * the corresponding value.
	 */
	public static <K,V> Collection<Pair<K,V>> mapToPairs(Map<K,V> map) {
		LinkedList<Pair<K,V>> result = new LinkedList<Pair<K,V>>();
		for (Map.Entry<K, V> e: map.entrySet()) {
			result.add(new Pair<K,V>(e.getKey(), e.getValue()));
		}
		return result;
	}

	/**
	 * Convert a collection of Pairs to a Map.
	 */
	@SafeVarargs
	public static <K,V> Map<K,V> pairsToMap(Pair<K,V>...pairs) {
		Map<K,V> result = new HashMap<K,V>();
		for (Pair<K,V> p: pairs) {
			result.put(p._1(), p._2());
		}
		return result;
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

}
