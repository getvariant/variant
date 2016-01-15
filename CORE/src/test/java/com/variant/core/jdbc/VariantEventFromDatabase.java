package com.variant.core.jdbc;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.collections4.Predicate;

public class VariantEventFromDatabase {

	long id;
	String sessionId;
	String name;
	String value;
	Date createdOn;
	Map<String,String> params;
    Collection<VariantEventVariantFromDatabase> eventVariants;
    
	VariantEventFromDatabase() {}
	
	public Long getId() { return id; }
	public String getEventName() { return name; }
	public String getEventValue() { return value; }
	public String getSessionId() { return sessionId; }
	public Date getCreatedOn() { return createdOn; }
	public Map<String,String> getParameterMap() { return params; }
	public Collection<VariantEventVariantFromDatabase> getEventVariants() { return eventVariants; }

	/**
	 * Predicate base selection.
	 * @param p
	 * @return
	 */
	public Collection<VariantEventVariantFromDatabase> getEventVariants(Predicate<VariantEventVariantFromDatabase> p) { 
		HashSet<VariantEventVariantFromDatabase> result = new HashSet<VariantEventVariantFromDatabase>();
		for (VariantEventVariantFromDatabase ee: eventVariants) if (p.evaluate(ee)) result.add(ee);
		return result; 	
	}

}
