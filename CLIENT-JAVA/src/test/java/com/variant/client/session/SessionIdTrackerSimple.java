package com.variant.client.session;

import com.variant.client.VariantInitParams;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;

/**
 *** Suitable for tests only. ***
 * A simple implementation of a session ID tracker.
 * No tracking in external state, simply treats first elem of the userData array
 * as the session ID.
 */
public class SessionIdTrackerSimple implements VariantSessionIdTracker {

	private String sessionId;

	/**
	 * Interpret userData as:
	 * 0    - session ID - String
	 * 1... - {@link VariantTargetingTracker.Entry} objects, if any
	 */
	@Override
	public void init(VariantInitParams initParams, Object... userData) {
		sessionId = (String) userData[0];
	}


	@Override
	public void set(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public void save(Object...userData) {
		
	}

	@Override
	public String get() {
		return sessionId;
	}
		
}
