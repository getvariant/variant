package com.variant.client.session;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.conn.ConnectionImpl;
import com.variant.client.impl.SessionImpl;

/**
 * Keep track of all sessions in a client instance in order to provide idempotency of the getSession() call.
 * Static singleton. Sessions from all connections are stored in this object.
 * 
 * 
 * @author Igor
 *
 */
public class SessionCache {

	private static Logger LOG = LoggerFactory.getLogger(SessionCache.class);
	
	//private static long DEFAULT_KEEP_ALIVE = DateUtils.MILLIS_PER_MINUTE * 30;
	
	private static long VACUUM_INTERVAL = DateUtils.MILLIS_PER_SECOND * 30;
		
	/**
	 */
	private class Entry {
		
		SessionImpl session;
		long accessTimestamp;
		
		Entry(SessionImpl session) {
			this.session = session;
			this.accessTimestamp = System.currentTimeMillis();
		}
		
		boolean isIdle() {
			return accessTimestamp + conn.getSessionTimeout()*1000 < System.currentTimeMillis();
		}
	}
	
	/**
	 * Vacuum thread.
	 * Expires and removes idle or stale sessions. 
	 * 
	 * @author Igor.
	 *
	 */
	private class VacuumThread extends Thread {
				
		@Override
		public void run() {
			
			boolean quit = false;
			
			while (true) {
				
				try {
					for (Iterator<Entry> iter = cache.values().iterator(); iter.hasNext();){
						Entry entry = iter.next();
						if (entry.isIdle()) {
							iter.remove();
							entry.session.expire();
						}
					}
										
					Thread.sleep(VACUUM_INTERVAL);

				}
				catch (InterruptedException e) {
					quit = true;
				}
				catch (Throwable t) {
					
					if (cache == null) {
						// Race condition with shutdown(). All we want is to quit.
						quit = true;
					}
					else {
						LOG.error("Unexpected exception in session cache vacuum thread.", t);
					}
				}
				
				if (quit || isInterrupted()) return;
			}
		}
	}

	private final ConnectionImpl conn;
	private ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<String, Entry>();
	private VacuumThread vacuumThread;

	/**
	 * 
	 */
	public SessionCache(Connection conn) {
		this.conn = (ConnectionImpl) conn;
		vacuumThread = new VacuumThread();
		vacuumThread.setDaemon(false);
		vacuumThread.setName(VacuumThread.class.getSimpleName());
		vacuumThread.start();
	}
	
	/**
	 * Add a new session to the cache.
	 * @param coreSession
	 */
	public void add(SessionImpl session) {
		cache.put(session.getId(), new Entry(session));
	}

	/**
	 * Get a session from cache. Touch access time.
	 * @param sessionId
	 * @return session object if found, null otherwise.
	 */
	public Session get(String sessionId) {
		
		Entry entry = cache.get(sessionId);
		if (entry != null) {
			entry.accessTimestamp = System.currentTimeMillis();
			return entry.session;
		}
		return null;
	}
		
	/**
	 * Expire locally a session that has already expired on the server.
	 * @param sessionId
	 */
	public void expire(String sessionId) {
		Entry entry = cache.remove(sessionId);
		if (entry != null) entry.session.expire();
	}
	
	/**
	 * Mark all sessions as expired and release underlying memory.
	 */
	public void shutdown() {
		vacuumThread.interrupt();
		cache = null;
	}
	
}
