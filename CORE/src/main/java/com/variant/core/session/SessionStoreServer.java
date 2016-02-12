package com.variant.core.session;

import java.util.HashMap;

import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;

/**
 * Remote session store accessible via a RESTFul API.
 * 
 * @author Igor
 *
 */
public class SessionStoreServer implements VariantSessionStore {

	private static VariantSessionIdTracker sidTracker = new SessionIdTrackerImpl();
		
	private HashMap<String, VariantSession> map = new HashMap<String, VariantSession>();

	/**
	 * A suitable implementation of session ID tracker TODO.... Probably not part of CORE.
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

	SessionStoreServer() { }
	
	/**
	 * @param session Session to save
	 * @param  userData The sid, which is assumed to be managed by the caller.
	 */
	@Override
	public void save(VariantSession session, Object...userData) {
		if (userData != null && userData.length > 0)
			throw new IllegalArgumentException("Not expecting userData");
		
		
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
