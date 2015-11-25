package com.variant.core;


/**
 * An implementation will use external mechanisms to obtain and to store
 * a stable session ID. For instance, in a web application environment,
 * session ID is read from and persisted in an HTTP cookie, just like an 
 * HTTP Session ID.
 * 
 * @author Igor
 */

public interface VariantSessionIdTracker {

	/**
	 * Read session ID. If the session ID did not exist in the backing persistence
	 * implementation, the implementation should create it. 
	 * @param userData implementation specific user data that caller can pass
	 *        to the implementation.
	 * 
	 * @return this should never return null.
	 */
	public String get(Object...userData);

	/**
	 * 
	 * @param userData implementation specific user data that caller can pass
	 *        to the implementation.
	 */
	public void save(String sessionId, Object...userData);
}

