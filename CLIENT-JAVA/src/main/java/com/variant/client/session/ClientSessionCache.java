package com.variant.client.session;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantSession;
import com.variant.client.impl.VariantSessionImpl;
import com.variant.core.VariantCoreSession;

/**
 * Keep track of all sessions in the JVM in order to provide idempotency of the getSession() call.
 * 
 * @author Igor
 *
 */
public class ClientSessionCache {

	private static Logger LOG = LoggerFactory.getLogger(ClientSessionCache.class);
	
	private static long DEFAULT_KEEP_ALIVE = DateUtils.MILLIS_PER_MINUTE * 30;
	private static long VACUUM_INTERVAL = DateUtils.MILLIS_PER_SECOND * 30;
	
	private ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<String, Entry>();
	private VacuumThread vacuumThread;
	
	/**
	 */
	private static class Entry {
		
		VariantSessionImpl session;
		long accessTimestamp;
		long keepAliveMillis;
		
		Entry(VariantSessionImpl session, long keepAliveMillis) {
			this.session = session;
			this.accessTimestamp = System.currentTimeMillis();
			this.keepAliveMillis = keepAliveMillis;
		}
		
		boolean isIdle() {
			return accessTimestamp + keepAliveMillis < System.currentTimeMillis();
		}
	}
	
	/**
	 */
	public ClientSessionCache() {
		vacuumThread = new VacuumThread();
		vacuumThread.setDaemon(false);
		vacuumThread.setName(VacuumThread.class.getSimpleName());
		vacuumThread.start();
	}
	
	/**
	 * Add a new session to the cache. Session came from the server so we know the keepAlive.
	 * @param coreSession
	 */
	public void add(VariantCoreSession session, long keepAlive) {
		cache.put(session.getId(), new Entry((VariantSessionImpl)session, keepAlive));
	}

	/**
	 * Add a new session to the cache. Session was created locally and does not yet have a keepAlive.
	 * We assign a long keepAlive, hoping it is greater than the timeout on the server. When it comes
	 * back from the server for the first time, keepAlive will be set properly.
	 * @param coreSession
	 */
	public void add(VariantSessionImpl session) {
		cache.put(session.getId(), new Entry(session, DEFAULT_KEEP_ALIVE));
	}

	/**
	 * Get a session from cache. Touch access time.
	 * @param sessionId
	 * @return session object if found, null otherwise.
	 */
	public VariantSession get(String sessionId) {
		
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
		cache = null;
	}
	
	/**
	 * Vacuum thread.
	 * Expires and removes idle sessions. 
	 * 
	 * @author Igor.
	 *
	 */
	private class VacuumThread extends Thread {
		
		@Override
		public void run() {
			
			boolean interruptedExceptionThrown = false;
			
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
					interruptedExceptionThrown = true;
				}
				catch (Throwable t) {
					LOG.error("Unexpected exception in session cache vacuum thread.", t);
				}
				
				if (interruptedExceptionThrown || isInterrupted()) return;
			}
			
		}
	}

}
