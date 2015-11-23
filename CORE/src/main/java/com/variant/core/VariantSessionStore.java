package com.variant.core;

/**
 * Session store is a map. Implementations will
 * be distributed, suitable for a particular application.
 * Testing of most of the code code could be done with
 * a simple local hash map.
 * 
 * @author Igor
 *
 */
public interface VariantSessionStore {

	/**
	 * Add a session to the store, replace if needed.
	 * @param key
	 * @param value
	 */
	public void put(String key, VariantSession value);	

	/**
	 * 
	 * @param key
	 * @return
	 */
	public VariantSession get(String key);
	
	/**
	 * Release memory resources, if no longer needed.
	 */
	public void shutdown();
}
