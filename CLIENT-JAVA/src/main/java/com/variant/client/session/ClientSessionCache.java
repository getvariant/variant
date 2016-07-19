package com.variant.client.session;

import com.variant.client.VariantSession;

/**
 * Keep track of all sessions in the JVM in order to provide idempotency of the getSession() call.
 * 
 * @author Igor
 *
 */
public class ClientSessionCache {

	private long sessionTimeoutMillis;

	/**
	 * Package instantiation
	 */
	ClientSessionCache(long sessionTimeoutMillis) {
		this.sessionTimeoutMillis = sessionTimeoutMillis;
	}
	
	/**
	 * Get a session from cache. Touch access time.
	 * @param sessionId
	 * @return session object if found, null otherwise.
	 */
	public VariantSession get(String sessionId) {
		return null;
	}
	
	public void put(VariantSession coreSession) {
		
	}
	
	/**
	 * Expire locally a session that has already expired on the server.
	 * @param sessionId
	 */
	public void expire(String sessionId) {
		
	}
	
	/**
	 * Mark all sessions as expired and release underlying memory.
	 */
	public void shutdown() {
		
	}
}
