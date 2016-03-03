package com.variant.core.session;

import com.variant.core.InitializationParams;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.exception.VariantInternalException;

/**
 * A simple implementation of a session ID tracker.
 * No tracking in external state, simply treats first elem of the userData array
 * as the session ID. Suitable for tests using the in-memory implementation of the session store.
 */
public class SessionIdTrackerSimple implements VariantSessionIdTracker {

	@Override
	public void initialized(InitializationParams initParams) throws Exception {}

	@Override
	public String get(Object...userData) {
		try {
			return (String) userData[0];
		}
		catch (Exception e) {
			throw new VariantInternalException(
					"User data object was of type " + userData.getClass().getName() + ", but expected String", e);
		}
	}

	@Override
	public void shutdown() {}
		
}
