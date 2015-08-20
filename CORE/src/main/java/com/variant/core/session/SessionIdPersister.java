package com.variant.core.session;


/**
 * An implementation will use external mechanisms to obtain and to store
 * a stable session ID. For instance, in a web application environment,
 * session ID is read from and persisted in an HTTP cookie, just like an 
 * HTTP Session ID.
 * 
 * @author Igor
 */

public interface SessionIdPersister {

	/**
	 * Read session id
	 * @param userData implementation specific user data that caller can pass
	 *        to the implementation.
	 * 
	 * @return
	 */
	public String get(Object userData);

	/**
	 * 
	 * @param userData implementation specific user data that caller can pass
	 *        to the implementation.
	 */
	public void persist(String sessionId, Object userData);
}
