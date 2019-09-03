package com.variant.core.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.variant.core.util.Tuples.Pair;



/**
 * 
 * @author Igor
 *
 */
final public class CollectionsUtils {

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

	@SuppressWarnings("unchecked")
	public static <K,V> HashMap<K,V> hashMap(Object...args) {
		if (args.length % 2 == 1)
			throw new IllegalArgumentException("Number of arguments must be even, but was " + args.length);
		
		HashMap<K, V> result = new HashMap<K,V>();
		
		for (int i = 0; i < args.length / 2; i++) result.put((K)args[i*2], (V)args[i*2+1]);
		
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

	/*----------------------------------------------------------------------------------*\
                                      toString()
   \*----------------------------------------------------------------------------------*/

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
	 */
	public static String toString(Object[] objectArray) {
		return toString(objectArray, ", ");
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
	 */
	public static String toString(Collection<?> c) {
		return toString(c, ", ");
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
	 */
	public static String toString(Map<?,?> map) {
		return toString(map, ", ");
	}
	
   /*----------------------------------------------------------------------------------*\
                                     equals()
   \*----------------------------------------------------------------------------------*/

	  /**
    * Assert that two lists are the same, including order.
    * Custom comparator.
    *  
    * @param 
    */
	public static <T> boolean equalAsLists(List<T> actual, List<T> expected, Comparator<T> comparator) {
      
      assertEquals("Actual list of size " + actual.size() + "was not equal expected size " + expected.size(), actual.size(), expected.size());
      
      Iterator<T> actualIter = actual.iterator(); 
      Iterator<T> expectedIter = expected.iterator(); 
      while (actualIter.hasNext()) {
         if (comparator.compare(actualIter.next(), expectedIter.next()) != 0) return false;
      }
      return true;
   }

   /**
    * Same as above with the natural comparator
    *  
    * @param 
    */
	public static <T> boolean equalAsLists(List<T> actual, List<T> expected) {

      Comparator<T> comp = new Comparator<T>() {
         @Override
         public int compare(Object o1, Object o2) {return o1.equals(o2) ? 0 : 1;}
      };
      return equalAsSets(actual, expected, comp);
   }

   /**
    * Same as above for varargs
    * @param actual
    * @param expected
    *
   protected <T> void assertEqualAsSets(Collection<T> actual, @SuppressWarnings("unchecked") T...expected) {
      assertEqualAsSets(actual, Arrays.asList(expected));
   }
   */

   /**
    * Same as above for maps
    * @param actual
    * @param expected
    */
	public static <K,V> boolean equalAsSets(Map<K,V> actual, Map<K,V> expected) {
      return equalAsSets(actual.entrySet(), expected.entrySet());
   }

   /**
    * Same as above with custom comparator over entries.
    * @param actual
    * @param expected
    */
	public static <K,V> boolean equalAsSets(Map<K,V> actual, Map<K,V> expected, Comparator<Map.Entry<K, V>> comp) {
      return equalAsSets(actual.entrySet(), expected.entrySet(), comp);
   }

   /**
    * Are two given collections set-equivalent, i.e. for each element in one, 
    * there's an equal element in the other.
    *  
    * @param comparator Custom comparator
    */
   public static <T> boolean equalAsSets(Collection<T> actual, Collection<T> expected, Comparator<T> comparator) {
      
      for (T a: actual) {
         boolean found = false;
         for (T e: expected) {
            if (comparator.compare(a,e) == 0) {
               found = true;
               break;
            }
         }
         if (!found) return false;
      }
      
      for (T e: expected) {
         boolean found = false;
         for (T a: actual) {
            if (comparator.compare(a,e) == 0) {
               found = true;
               break;
            }
         }
         if (!found) return false;
      }
      
      return true;
   }

   /**
    * Same as above with the natural comparator
    *  
    * @param 
    */
   public static <T> boolean equalAsSets(Collection<T> actual, Collection<T> expected) {

      Comparator<T> comp = new Comparator<T>() {
         @Override
         public int compare(Object o1, Object o2) {return o1.equals(o2) ? 0 : 1;}
      };
      
      return equalAsSets(actual, expected, comp);
   }

}
