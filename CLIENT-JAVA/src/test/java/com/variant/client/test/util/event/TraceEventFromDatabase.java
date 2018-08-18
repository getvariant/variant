package com.variant.client.test.util.event;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class TraceEventFromDatabase {
	
	public final Long id;
	public final String sessionId;
	public final Date createdOn;
	public final String name;

	public final HashMap<String,String> attributes = new HashMap<String,String>();
	public final HashSet<EventExperienceFromDatabase> eventExperiences = new HashSet<EventExperienceFromDatabase>();

	TraceEventFromDatabase (Long id, String sessionId, Date createdOn, String name) {
		this.id = id;
		this.sessionId = sessionId;
		this.createdOn = createdOn;
		this.name = name;
	}

    	
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
	
	@Override
	public String toString() {

		final StringWriter body = new StringWriter(1024);
		
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeNumberField("id", id);
			jsonGen.writeNumberField("createdOn", createdOn.getTime());
			jsonGen.writeStringField("name", name);
			if (attributes.size() > 0) {
				jsonGen.writeObjectFieldStart("attrs");
				for (Map.Entry<String, String> attr: attributes.entrySet()) {
					jsonGen.writeStringField(attr.getKey(), attr.getValue());
				}
				jsonGen.writeEndObject();
			}
			if (eventExperiences.size() > 0) {
				jsonGen.writeArrayFieldStart("expList");
				for (EventExperienceFromDatabase ee: eventExperiences) {
					jsonGen.writeRaw(ee.toString());
				}
				jsonGen.writeEndArray();
				
			}
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw new RuntimeException("Yikes", e);
		}
		
		return body.toString();
	}
}
