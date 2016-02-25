package com.variant.core.session;

import java.util.HashMap;
import java.util.Map;

import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;

/**
 * Simplest session store implementation in local memory.
 * No external session ID tracking is assumed. Instead, session IDs
 * are passed in as user data. Sessions are stored in a map keyed by ID,
 * no expiration.
 * 
 *** Good for tests only. ***
 * 
 * @author Igor
 *
 */
public class SessionStoreLocalMemory implements VariantSessionStore {

	private static VariantSessionIdTracker sidTracker = new SessionIdTrackerImpl();
		
	private HashMap<String, VariantSession> map = new HashMap<String, VariantSession>();

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
			
	};

	SessionStoreLocalMemory() { }
	
	@Override
	public void initialized(Map<String, String> initParams) {}

	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 */
	@Override
	public void save(VariantSession session, Object...userData) {
			map.put(sidTracker.get(userData), session);
	}

	/**
	 * 
	 */
	@Override
	public VariantSessionIdTracker getSessionIdTracker() {
		return sidTracker;
	}

	/**
	 * @param  userData is the sid, which is assumed to be managed by the caller.
	 */
	@Override
	public VariantSession get(Object...userData) {
			return map.get(sidTracker.get(userData));
	}
	
	@Override
	public void shutdown() {
		map = null;
	}

}
