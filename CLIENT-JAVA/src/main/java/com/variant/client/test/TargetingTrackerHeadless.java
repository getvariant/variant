package com.variant.client.test;

import java.util.HashSet;
import java.util.Set;

import com.variant.client.impl.TargetingTrackerEntryImpl;
import com.variant.client.impl.TargetingTrackerString;

/**
 * A headless implementation of the targeting tracker.
 * No tracking in external state, simply saves targeted experiences in an in-memory string buffer.
 */
public class TargetingTrackerHeadless extends TargetingTrackerString {

	private String buffer = null;
		
	/**
	 * Interpret userData as:
	 * 0    - session ID - String
	 * 1... - String "testName.expName"
	 */
	@Override
	public void init(Object...userData) {
		Set<Entry> entries = new HashSet<Entry>(userData.length);
		for (int i = 0; i < userData.length; i++) {
			if (i > 0) {
				String[] tokens = ((String)userData[i]).split("\\.");
				entries.add(new TargetingTrackerEntryImpl(System.currentTimeMillis(), tokens[0], tokens[1]));
			}
		}
		set(entries);
	}

	@Override
	public void save(Object... userData) {

	}

	@Override
	public Set<Entry> get() {
		return fromString(buffer);
	}

	@Override
	public void set(Set<Entry> entries) {
		buffer = toString(entries);
	}
		
}
