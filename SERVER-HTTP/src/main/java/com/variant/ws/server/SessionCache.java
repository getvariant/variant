package com.variant.ws.server;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.session.VariantSessionImpl;
import com.variant.ws.server.core.VariantCore;

/**
 * Sessions are stored serialized as JSON strings because most of the time
 * we won't need them deserialized on the server. 
 * @author Igor
 *
 */
public class SessionCache {

	private static final Logger LOG = LoggerFactory.getLogger(SessionCache.class);
	
	private static ConcurrentHashMap<String, Entry> cacheMap = new ConcurrentHashMap<>();
	
	static {
		VacuumThread vt = new VacuumThread();
		vt.setName("VrntSessionVacuum");
		vt.setDaemon(true);
		vt.start();
	}
	
	/**
	 * Background thread deletes cache entries older than the keep-alive interval.
	 */
	private static class VacuumThread extends Thread {
		
		// How frequently should we vacuum?
		private static long vacuumingFrequencyMillis;
		private static long sessionTimeoutMillis;

		private VacuumThread() {
			
			// Session expiration interval is defined in the session.store.class.init map
			// and is also used by the client.
			InitializationParams params = VariantCore.api().getProperties().get(VariantProperties.Key.SESSION_STORE_CLASS_INIT, InitializationParams.class);
			sessionTimeoutMillis = (Integer) params.getOr("sessionTimeoutSecs",  new Integer(15 * 60) /*15 min default*/) * 1000;
			// Vacuuming frequency is same place but only used on the server.
			vacuumingFrequencyMillis = (Integer) params.getOr("vacuumingFrequencySecs",  new Integer(60) /*1 min default*/)  * 1000;
		}

		@Override
		public void run() {

			LOG.debug("Vacuuming thread " + Thread.currentThread().getName() + " started.");
			
			boolean interrupted = false;
			boolean timeToGo = false;
			
			while (!timeToGo) {
			
				try {
					long now = System.currentTimeMillis();					
					Iterator<Map.Entry<String, Entry>> iter = cacheMap.entrySet().iterator();
					while(iter.hasNext()) {
						Map.Entry<String, Entry> e = iter.next();
						if (sessionTimeoutMillis > 0 && e.getValue().lastAccessTimestamp + sessionTimeoutMillis < now) {
							iter.remove();
							if (LOG.isTraceEnabled()) LOG.trace(String.format("Vacuumed expired session ID [%s]", e.getKey()));
						}
					}
					
					sleep(vacuumingFrequencyMillis);

				}
				catch (InterruptedException e) {
					interrupted = true;
				}
				catch (Throwable t) {
					LOG.error("Unexpected exception in vacuuming thread.", t);
				}
				
				if (interrupted || isInterrupted()) {
					if (LOG.isDebugEnabled())
						LOG.debug("Vacuuming thread " + Thread.currentThread().getName() + " interrupted and exited.");
					cacheMap = null;
					timeToGo = true;
				}
			}
		}
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
}
