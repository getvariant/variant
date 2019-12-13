package com.variant.share.util;

import java.util.HashMap;
import java.util.Map;

import com.variant.share.util.Tuples.Pair;

public class CaseInsensitiveMap<V> extends HashMap<String, V> {

	/**
	 * 
	 */
	public CaseInsensitiveMap() {
		super();
	}
	
	public CaseInsensitiveMap(Map<String, V> map) {
		for (Map.Entry<String, V> e: map.entrySet()) {
			put(e.getKey(), e.getValue());
		}
	}
	
	/**
	 * 
	 */
	@Override
	public V put(String key, V value) {
		return super.put(key.toUpperCase(), value);
	}
	
	/**
	 * Treat Pair as (key,value) tuple.
	 * @param pair
	 * @return
	 */
	public V put(Pair<String, V> pair) {
		return super.put(pair._1().toUpperCase(), pair._2());
	}

	/**
	 * In 8, implementation of HashMap.putAll() changed to go directly into the
	 * table, around the put() method. We need to override that in order to explicitly
	 * invoke the put() method to cause the upper-casing.
	 */
	@Override
	public void putAll(Map<? extends String, ? extends V> map) {
		for (Map.Entry<? extends String, ? extends V> e: map.entrySet()) {
			super.put(e.getKey().toUpperCase(), e.getValue());
		}
	}

	@Override
	public V get(Object key) {
		return super.get(((String)key).toUpperCase());
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
}
