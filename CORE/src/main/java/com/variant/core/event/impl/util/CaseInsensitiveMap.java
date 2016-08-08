package com.variant.core.event.impl.util;

import java.util.HashMap;
import java.util.Map;

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
		return super.put (key.toUpperCase(), value);
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
