package com.variant.server;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.variant.core.VariantSession;
import com.variant.core.session.VariantSessionImpl;
import com.variant.core.util.VariantStringUtils;

/**
 * Sessions are stored serialized as JSON strings because most of the time
 * we won't need them deserialized on the server.
 * @author Igor
 *
 */
public class SessionStore {

	private static Random random = new Random(System.currentTimeMillis());
	private static ConcurrentHashMap<String, Entry> sessionMap = new ConcurrentHashMap<>();

	/**
	 * 
	 * @param ssn
	 */
	public static void add(String id, String payload) {
		sessionMap.put(id, new Entry(payload));
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public static VariantSession get(String key, boolean create) {

		VariantSession result = sessionMap.get(key);
		if (result == null && create) {
			String newId = VariantStringUtils.random64BitString(random);
			result = new VariantSessionImpl(newId);
			sessionMap.put(newId, result);
		}
		return result;
	}
	
	/**
	 * @author Igor
	 */
	private static class Entry {
		private String payload;
		private long lastAccessTimestamp;
		
		private Entry(String payload) {
			this.payload = payload;
			this.lastAccessTimestamp = System.currentTimeMillis();
		}
	}
}
