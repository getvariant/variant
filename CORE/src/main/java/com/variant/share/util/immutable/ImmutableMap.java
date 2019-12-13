package com.variant.share.util.immutable;

import java.util.HashMap;
import java.util.Map;

public class ImmutableMap<K,V> extends HashMap<K,V> {

	public ImmutableMap() {
		super();
	}
	
	public ImmutableMap(Map<K, V> map) {
		super.putAll(map);
	}
	
	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException("Cannot modify map.");
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		throw new UnsupportedOperationException("Cannot modify map.");
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
}
