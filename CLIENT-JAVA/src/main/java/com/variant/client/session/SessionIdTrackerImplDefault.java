package com.variant.client.session;

import com.variant.client.VariantClientPropertyKeys;
import com.variant.client.VariantInitParams;
import com.variant.client.VariantSessionIdTracker;

/**
 * Unusable default implementation of {@link VariantSessionIdTracker}. 
 * Must be overridden.
 */
public class SessionIdTrackerImplDefault implements VariantSessionIdTracker {

	private final static String MESSAGE = 
			"Supply a functional implementation in applicaiton property " + 
					VariantClientPropertyKeys.SESSION_ID_TRACKER_CLASS_NAME.propertyName();
	@Override
	public void init(VariantInitParams initParams, Object...userData) {}

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
