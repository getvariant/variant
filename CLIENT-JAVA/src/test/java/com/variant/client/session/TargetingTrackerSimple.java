package com.variant.client.session;

import java.util.ArrayList;
import java.util.Collection;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.client.VariantTargetingTracker;
import com.variant.core.VariantProperties;

/**
 *** Suitable for tests only. ***
 * A simple implementation of the targeting tracker.
 * No tracking in external state, simply saves targeted experiences in an in-memory string buffer.
 */
public class TargetingTrackerSimple extends TargetingTrackerString {

	private VariantClient client = null;
	private String buffer = null;
		
	/**
	 * Interpret userData as:
	 * 0    - session ID - String
	 * 1... - {@link VariantTargetingTracker.Entry} objects, if any
	 */
	@Override
	public void init(VariantInitParams initParams, Object...userData) {
		client = initParams.getVariantClient();
		Collection<Entry> entries = new ArrayList<Entry>(userData.length);
		for (int i = 0; i < userData.length; i++) {
			if (i > 0)	entries.add((Entry)userData[i]);
		}
		set(entries);
	}

	@Override
	public void save(Object... userData) {
		//throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Entry> get() {
		return fromString(buffer, client.getSchema());
	}

	@Override
	public void set(Collection<Entry> entries) {
		buffer = toString(entries);
	}

	@Override
	protected Properties getProperties() {
		return client.getProperties();
	}
		
}
