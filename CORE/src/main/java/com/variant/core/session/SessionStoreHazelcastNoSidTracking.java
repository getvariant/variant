package com.variant.core.session;

import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;

/**
 * Potentially distributed session store built on
 * Hazelcast (hazelcast.org) distributed cache.
 * Without session tracking: client manages and passes in session IDs.
 * If this ever becomes real distributed session store, we'll also need to
 * change session tracking implementation.
 * 
 * Not currently used but should be periodically tested.
 * 
 * @author Igor
 *
 */
public class SessionStoreHazelcastNoSidTracking implements VariantSessionStore {

	private static VariantSessionIdTracker sidTracker = new SessionIdTrackerImpl();
	
	private HazelcastInstance instance = null;
	private static final String MAP_NAME = "localSessionStoreMap";
	
	/**
	 * A suitable implementation of session ID tracker.
	 */
	private static class SessionIdTrackerImpl implements VariantSessionIdTracker {

		@Override
		public String get(Object...userData) {
			try {
				return (String) userData[0];
			}
			catch (ClassCastException e) {
				throw new RuntimeException(
						"User data object was of type " + userData.getClass().getName() + ", but expected String", e);
			}
		}
		
		@Override
		public void save(String sessionId, Object...userData) {
			// nothing.				
		}
	
	};

	SessionStoreHazelcastNoSidTracking() {
		instance = Hazelcast.newHazelcastInstance();
	}
		
	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 */
	@Override
	public void save(VariantSession session, Object...userData) {
			Map<String, VariantSession> store = instance.getMap(MAP_NAME);
			store.put(sidTracker.get(userData), session);
	}

	/**
	 * @param  userData is the sid, which is assumed to be managed by the caller.
	 */
	@Override
	public VariantSession get(Object...userData) {
			Map<String, VariantSession> store = instance.getMap(MAP_NAME);
			return store.get(sidTracker.get(userData));
	}
	
	/**
	 * 
	 */
	@Override
	public VariantSessionIdTracker getSessionIdTracker() {
		return sidTracker;
	}

	@Override
	public void shutdown() {
		instance.shutdown();
	}

}
