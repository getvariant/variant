package com.variant.server;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantProperties;
import com.variant.core.impl.CoreSessionImpl;

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
			
			VariantProperties props = ServerBoot.getCore().getProperties();
			sessionTimeoutMillis = props.get(ServerPropertyKeys.SESSION_TIMEOUT_SECS, Integer.class) * 1000;
			vacuumingFrequencyMillis = props.get(ServerPropertyKeys.SESSION_STORE_VACUUM_INTERVAL_SECS, Integer.class) * 1000;
		}

		@Override
		public void run() {

			if (LOG.isDebugEnabled())
				LOG.info("Vacuuming thread " + Thread.currentThread().getName() + " started.");
			
			boolean interrupted = false;
			
			while (true) {
							
				
				try {
					long now = System.currentTimeMillis();
					int count = 0;
					Iterator<Map.Entry<String, Entry>> iter = cacheMap.entrySet().iterator();
					while(iter.hasNext()) {
						Map.Entry<String, Entry> e = iter.next();
						if (sessionTimeoutMillis > 0 && e.getValue().lastAccessTimestamp + sessionTimeoutMillis < now) {
							iter.remove();
							count++;
							if (LOG.isTraceEnabled()) 
								LOG.trace(String.format("Vacuumed expired session ID [%s]", e.getKey()));
						}
					}
					
					if (LOG.isDebugEnabled()) LOG.debug(String.format("Vacuumed %s session(s)", count));

					sleep(vacuumingFrequencyMillis);

				}
				catch (InterruptedException e) {
					interrupted = true;
				}
				catch (Throwable t) {
					LOG.error("Unexpected exception in vacuuming thread.", t);
				}
				
				if (interrupted || isInterrupted()) {
					LOG.info("Vacuuming thread " + Thread.currentThread().getName() + " interrupted and exited.");
					cacheMap = null;
					return;
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
		private CoreSessionImpl session = null;
		private long lastAccessTimestamp = System.currentTimeMillis();

		/**
		 * @param json
		 */
		private Entry(byte[] json) { this.json = json;}

		//private Entry(String json) { this.json = json.getBytes();}
	
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
		public CoreSessionImpl getSession() {
			if (session == null && json != null) {
				//session = new SessionServerWrapper(VariantSessionImpl.fromJson(VariantCore.api(), new String(json)));
				session = CoreSessionImpl.fromJson(ServerBoot.getCore(), new String(json));
			}
			return session;
		}
	}
}
