package com.variant.server.test.util;

import java.util.Collection;
import java.util.Date;
import java.util.Map;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class TraceEventFromDatabase {

	long id;
	String sessionId;
	String name;
	String value;
	Date createdOn;
	Map<String,String> params;
    Collection<EventExperienceFromDatabase> eventExperiences;
    
	TraceEventFromDatabase() {}
	
	public Long getId() { return id; }
	public String getName() { return name; }
	public String getValue() { return value; }
	public String getSessionId() { return sessionId; }
	public Date getCreatedOn() { return createdOn; }
	public Map<String,String> getParameterMap() { return params; }
	public Collection<EventExperienceFromDatabase> getEventExperiences() { return eventExperiences; }

	/** ********************
	 * Predicate base selection.
	 * @param p
	 * @return
	 * Breaks compilation because we no longer have apache commons Predicate.
	 * If this is actually needed, replace with Scala class make this method take a function param.
	public Collection<EventExperienceFromDatabase> getEventExperiences(Predicate<EventExperienceFromDatabase> p) { 
		HashSet<EventExperienceFromDatabase> result = new HashSet<EventExperienceFromDatabase>();
		for (EventExperienceFromDatabase ee: eventExperiences) if (p.evaluate(ee)) result.add(ee);
		return result; 	
	}
    ***********************/
	
	@Override public String toString() {
		ObjectMapper jacksonMapper = new ObjectMapper();
		jacksonMapper.enable(SerializationFeature.INDENT_OUTPUT);
		try {
			return jacksonMapper.writeValueAsString(this);
		}
		catch (Exception e) {
			throw new RuntimeException("Unable to serialize object", e);
		}
	}
}
