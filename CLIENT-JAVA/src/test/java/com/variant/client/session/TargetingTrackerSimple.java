package com.variant.client.session;

import java.util.HashSet;
import java.util.Set;

import com.variant.client.Session;
import com.variant.core.schema.Test.Experience;

/**
 *** Suitable for tests only. ***
 * A simple implementation of the targeting tracker.
 * No tracking in external state, simply saves targeted experiences in an in-memory string buffer.
 */
public class TargetingTrackerSimple extends TargetingTrackerString {

	private String buffer = null;
	private Session session;
		
	/**
	 * Interpret userData as:
	 * 0    - session ID - String
	 * 1... - Test.Experience objects, if any
	 */
	@Override
	public void init(Session session, Object...userData) {
		Set<Entry> entries = new HashSet<Entry>(userData.length);
		for (int i = 0; i < userData.length; i++) {
			if (i > 0) 
				entries.add(new TargetingTrackerEntryImpl((Experience)userData[i], System.currentTimeMillis(), session));
		}
		set(entries);
		this.session = session;
	}

	@Override
	public void save(Object... userData) {
		//throw new UnsupportedOperationException();
	}

	@Override
	public Set<Entry> get() {
		return fromString(buffer);
	}

	@Override
	public void set(Set<Entry> entries) {
		buffer = toString(entries);
	}

	@Override
	protected Session getSession() {
		return session;
	}

		
}
