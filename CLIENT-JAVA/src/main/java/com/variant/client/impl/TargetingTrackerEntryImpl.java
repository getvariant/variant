package com.variant.client.impl;

import com.variant.client.TargetingTracker;
import com.variant.share.session.SessionScopedTargetingStabile;

public class TargetingTrackerEntryImpl implements TargetingTracker.Entry {
		
	private final String testName, experienceName;
	private final long timestamp;
	
	/**
	 * 
	 * @param experience
	 * @param timestamp
	 */
	public TargetingTrackerEntryImpl(long timestamp, String testName, String experienceName) { 
		this.testName = testName;
		this.experienceName = experienceName;
		this.timestamp = timestamp;
	}
	
	/**
	 * 
	 * @param stabileEntry
	 */
	public TargetingTrackerEntryImpl(SessionScopedTargetingStabile.Entry stabileEntry) { 
		this.testName = stabileEntry.getTestName();
		this.experienceName = stabileEntry.getExperienceName();
		this.timestamp = stabileEntry.getTimestamp();
	}

	
	/**
	 */
	@Override
	public String getTestName() {
		return testName;
	}
	
	/**
	 */
	@Override
	public String getExperienceName() {
		return experienceName;
	}

	/**
	 */
	@Override
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 */
	@Override
	public String toString() {
		return timestamp + "." + testName + "." + experienceName;
	}

}
