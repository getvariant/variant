package com.variant.client.session;

import com.variant.client.VariantTargetingTracker;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;

public class TargetingTrackerEntryImpl implements VariantTargetingTracker.Entry {
		
	private String testName, experienceName;
	private long timestamp;
	
	/**
	 * 
	 * @param experience
	 * @param timestamp
	 */
	public TargetingTrackerEntryImpl(Experience experience, long timestamp) { 
		this.testName = experience.getTest().getName();
		this.experienceName = experience.getName();
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
	public Experience getAsExperience(Schema schema) {
		Test test = schema.getTest(testName);
		return test == null ? null : test.getExperience(experienceName);
	}
	
	/**
	 */
	@Override
	public long getTimestamp() {return timestamp;}
	
	@Override
	public String getTestName() {
		return testName;
	}

	@Override
	public String getExperienceName() {
		return experienceName;
	}
	
	@Override
	public String toString() {
		return timestamp + "." + testName + "." + experienceName;
	}

}