package com.variant.core.test.jdbc;


public class EventExperienceFromDatabase {

	long id;
	long eventId;
	String testName;
	String experienceName;
	boolean isControl;
	    
    EventExperienceFromDatabase() {}
    
	public long getId() { return id; }
	public long getEventId() { return eventId; }
	public String getTestName() { return testName; }
	public String getExperienceName() { return experienceName; }
	public boolean isControl() { return isControl; }

}
