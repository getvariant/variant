package com.variant.client.session;

import com.variant.client.ConfigKeys;
import com.variant.client.Connection;
import com.variant.client.VariantSessionIdTracker;

/**
 * Unusable default implementation of {@link VariantSessionIdTracker}. 
 * Must be overridden.
 */
public class SessionIdTrackerDefault implements VariantSessionIdTracker {

	private final static String MESSAGE = 
			"Supply a functional implementation in applicaiton property " + 
					ConfigKeys.SESSION_ID_TRACKER_CLASS_NAME;
	@Override
	public void init(Connection conn, Object...userData) {}

	@Override
	public void save(Object... userData) {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public String get() {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public void set(String sessionId) {
		throw new UnsupportedOperationException(MESSAGE);
	}

}
