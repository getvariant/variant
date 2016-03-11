package com.variant.ws.server;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.variant.core.VariantSession;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.session.VariantSessionImpl;
import com.variant.ws.server.core.VariantCore;

/**
 * Sessions are stored serialized as JSON strings because most of the time
 * we won't need them deserialized on the server. 
 * @author Igor
 *
 */
public class SessionCache {

	private static ConcurrentHashMap<String, Entry> cacheMap = new ConcurrentHashMap<>();
	private static boolean valid = false;

	static {
		VacuumThread vt = new VacuumThread();
		vt.setName("ssn_cache_vac");
		vt.setDaemon(true);
		vt.start();
		valid = true;
	}
	
	/**
	 * @param ssn
	 */
	public static Entry put(String key, byte[] json) {
		return cacheMap.put(key, new Entry(json));
	}
	
	/**
	 * @param ssn
	 */
	public static Entry put(String key, String json) {
		return put(key, json.getBytes());
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public static Entry get(String key) throws Exception {

		if (!valid) throw new VariantInternalException("SessionCache has been invalidated");
		
		Entry result = cacheMap.get(key);
		if (result != null) result.lastAccessTimestamp = System.currentTimeMillis();
		return result;
	}
	
	/**
	 * @author Igor
	 */
	public static class Entry {
		// Store as json and only deserialize if needed.
		private byte[] json = null;
		private VariantSession session = null;
		private long lastAccessTimestamp = System.currentTimeMillis();

		/**
		 * @param json
		 */
		private Entry(byte[] json) { this.json = json;}

		private Entry(String json) { this.json = json.getBytes();}
	
		/**
		 * Raw payload.
		 * @return
		 */
		public byte[] getJson() {
			return json;
		}
		
		/**
		 * Lazily deserialize from json, if we have it.
		 * @return
		 */
		public VariantSession getSession() {
			if (session == null && json != null) {
				session = VariantSessionImpl.fromJson(VariantCore.api(), new String(json));
			}
			return session;
		}
	}

	/**
	 * Background thread deletes cache entries older than the keep-alive interval.
	 */
	private static class VacuumThread extends Thread {
		
		// Entry will be removed from cache after this period
		private static final long KEEP_ALIVE_MILLIS = 15 * 60 * 1000; // 15 min.

		// How frequently should the thread run?
		private static final long PAUSE_MILLIS = 60 * 1000;  // 1 min.
		
		@Override
		public void run() {
			
			try {
				sleep(PAUSE_MILLIS);
			} catch (InterruptedException e1) {
				valid = false;
				return;
			}
			
			long now = System.currentTimeMillis();
			Iterator<Map.Entry<String, Entry>> iter = cacheMap.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<String, Entry> e = iter.next();
				if (e.getValue().lastAccessTimestamp + KEEP_ALIVE_MILLIS > now) 
					iter.remove();
			}
		}
	}
}
