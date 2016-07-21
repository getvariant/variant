package com.variant.client.session;

import java.util.HashMap;

import com.variant.client.VariantClient;
import com.variant.client.VariantSession;

/**
 * Keep track of all sessions in the JVM in order to provide idempotency of the getSession() call.
 * 
 * @author Igor
 *
 */
public class ClientSessionCache {

	private VariantClient client = null;
	private HashMap<String, Entry> map = new HashMap<String, Entry>();
	
	/**
	 */
	private static class Entry {
		VariantSession session;
		long accessTimestamp;
		private Entry(VariantSession session) {
			this.session = session;
			accessTimestamp = System.currentTimeMillis();
		}
	}
	
	/**
	 */
	public ClientSessionCache(VariantClient client) {
		this.client = client;
	}
	
	/**
	 * 
	 * @param coreSession
	 */
	public void put(VariantSession session) {
		map.put(session.getId(), new Entry(session));
	}

	/**
	 * Get a session from cache. Touch access time.
	 * @param sessionId
	 * @return session object if found, null otherwise.
	 */
	public VariantSession get(String sessionId) {
		return null;
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
