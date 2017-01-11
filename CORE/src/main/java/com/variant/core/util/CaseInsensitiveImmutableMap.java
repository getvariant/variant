package com.variant.core.util;

import java.util.Map;

public class CaseInsensitiveImmutableMap<V> extends CaseInsensitiveMap<V> {

	public CaseInsensitiveImmutableMap(Map<String, V> map) {
		for (Map.Entry<String, V> e: map.entrySet()) {
			super.put(e.getKey().toUpperCase(), e.getValue());
		}
	}
	
	@Override
	public V put(String key, V value) {
		throw new UnsupportedOperationException("Cannot modify map.");
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
