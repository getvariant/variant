package com.variant.client.session;

import com.variant.client.Session;
import com.variant.client.TargetingTracker;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.SessionScopedTargetingStabile;

public class TargetingTrackerEntryImpl implements TargetingTracker.Entry {
		
	private Session session;
	private String testName, experienceName;
	private long timestamp;
	
	/**
	 * 
	 * @param experience
	 * @param timestamp
	 */
	public TargetingTrackerEntryImpl(Experience experience, long timestamp, Session session) { 
		this.testName = experience.getTest().getName();
		this.experienceName = experience.getName();
		this.timestamp = timestamp;
		this.session = session;
	}
	
	/**
	 * 
	 * @param stabileEntry
	 */
	public TargetingTrackerEntryImpl(SessionScopedTargetingStabile.Entry stabileEntry, Session session) { 
		this.testName = stabileEntry.getTestName();
		this.experienceName = stabileEntry.getExperienceName();
		this.timestamp = stabileEntry.getTimestamp();
		this.session = session;
	}

	/**
	 */
	@Override
	public Experience getAsExperience() {
		Test test = session.getConnection().getSchema().getTest(testName);
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
