package com.variant.client.test;

import com.variant.client.SessionIdTracker;

/**
 * A headless implementation of a session ID tracker.
 * No tracking in external state, simply treats first element of the userData array as the session ID.
 */
public class SessionIdTrackerHeadless implements SessionIdTracker {

	private String sessionId;

	/**
	 */
	@Override
	public void init(Object... userData) {
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
