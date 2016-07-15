package com.variant.client;

import com.variant.core.exception.VariantInternalException;

/**
 * A simple implementation of a session ID tracker.
 * No tracking in external state, simply treats first elem of the userData array
 * as the session ID. Suitable for tests only.
 */
public class SessionIdTrackerSimple implements VariantSessionIdTracker {

	@Override
	public void initialized(VariantInitParams initParams) throws Exception {}

	@Override
	public void save(Object... userData) {}

	@Override
	public String get(Object...userData) {
		try {
			return (String) userData[0];
		}
		catch (ClassCastException e) {
			throw new VariantInternalException(
					"User data object was of type " + userData.getClass().getName() + ", but expected String");
		}
	}
		
}
