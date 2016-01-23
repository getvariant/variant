package com.variant.core.jdbc;


public class VariantEventVariantFromDatabase {

	long id;
	long eventId;
	String testName;
	String experienceName;
	boolean isExperienceControl;
	boolean isStateNonvariant;
	    
    VariantEventVariantFromDatabase() {}
    
	public long getId() { return id; }
	public long getEventId() { return eventId; }
	public String getTestName() { return testName; }
	public String getExperienceName() { return experienceName; }
	public boolean isExperienceControl() { return isExperienceControl; }
	public boolean isStateNonvariant() { return isStateNonvariant; }

}
