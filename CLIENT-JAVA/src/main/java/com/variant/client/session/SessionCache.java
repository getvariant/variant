package com.variant.client.session;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.Session;
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
			
	/**
	 */
	private class Entry {
		
		SessionImpl session;
		long accessTimestamp;
		
		Entry(SessionImpl session) {
			this.session = session;
			this.accessTimestamp = System.currentTimeMillis();
		}
		/*
		boolean isIdle() {
			return accessTimestamp + sessionTimeoutMillis < System.currentTimeMillis();
		}
		*/
	}
	
	/**
	 * Vacuum thread.
	 * Expires and removes idle or stale sessions. 
	 * NOT NEEDED AFTER #114 (0.8.1)
	 * @author Igor.
	 *
	 *
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
										
					Thread.sleep(vacuumInterval);

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
	*/

	//private final long sessionTimeoutMillis;
	//private final long vacuumInterval;
	
	private ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<String, Entry>();
	// private VacuumThread vacuumThread;

	// ---------------------------------------------------------------------------------------------//
	//                                             PUBLIC                                           //
	// ---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	public SessionCache() {
	/*
		this.sessionTimeoutMillis = sessionTimeoutMillis;
		// Vacuum therad wakes up no less frequently than 30 seconds, but more frequently for tests,
		// when the timeout is set low, e.g. 1 sec.
		this.vacuumInterval = Math.min(sessionTimeoutMillis / 2, TimeUtils.MILLIS_PER_SECOND * 30);
		vacuumThread = new VacuumThread();
		vacuumThread.setDaemon(false);
		vacuumThread.setName(VacuumThread.class.getSimpleName());
		vacuumThread.start();
	 */
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
	public Session get(String sid) {
		
		Session result = null;
		Entry entry = cache.get(sid);

		if (entry == null) {
			if (LOG.isDebugEnabled()) 
				LOG.debug("Local cache miss for sid [" + sid + "]");
		}
		else {
			if (LOG.isDebugEnabled()) 
				LOG.debug("Local cache hit for sid [" + sid + "]");
			entry.accessTimestamp = System.currentTimeMillis();
			return entry.session;
		}
		return result;
	}
		
	/**
	 * Expire locally a session that has already expired on the server.
	 * @param sessionId
	 */
	public void expire(String sid) {
		if (LOG.isDebugEnabled()) 
			LOG.debug("Expiring local session [" + sid + "]");

		Entry entry = cache.remove(sid);
		if (entry != null) entry.session.expire();
	}
	
	/**
	 * Mark all sessions as expired and release underlying memory.
	 */
	public void destroy() {
		//vacuumThread.interrupt();
		for (Entry e: cache.values()) {e.session.expire();}
		cache.clear();
	}

}
