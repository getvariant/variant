package com.variant.client.session;

import java.util.Collection;

import com.variant.client.ConfigKeys;
import com.variant.client.Connection;
import com.variant.client.VariantTargetingTracker;

/**
 * Unusable default implementation of {@link VariantTargetingTracker}. 
 * Must be overridden.
 * 
 * @author Igor
 *
 */
public class TargetingTrackerDefault implements VariantTargetingTracker {
	
	private final static String MESSAGE = 
			"Supply a functional implementation in applicaiton property " + 
					ConfigKeys.TARGETING_TRACKER_CLASS_NAME;

	@Override
	public void init(Connection conn, Object...userData) {}		

	@Override
	public void save(Object...userData) {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public Collection<Entry> get() {
		throw new UnsupportedOperationException(MESSAGE);
	}

	@Override
	public void set(Collection<Entry> entries) {
		throw new UnsupportedOperationException(MESSAGE);
	}

}
