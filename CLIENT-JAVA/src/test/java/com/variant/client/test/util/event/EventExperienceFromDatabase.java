package com.variant.client.test.util.event;

import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Represents a tuple from the EVENT_EXPERIENCES table.
 */
   
public class EventExperienceFromDatabase {

	public final Long id;
	public final Long eventId;
	public final String testName;
	public final String experienceName;
	public final Boolean isControl;
	
	EventExperienceFromDatabase (
		Long id, Long eventId, String testName, String experienceName, Boolean isControl) {   
		this.id = id;
		this.eventId = eventId;
		this.testName = testName;
		this.experienceName = experienceName;
		this.isControl = isControl;

	}
	
	@Override
	public String toString() {

		final StringWriter body = new StringWriter(1024);
		
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeNumberField("id", id);
			jsonGen.writeNumberField("eventId", eventId);
			jsonGen.writeStringField("testName", testName);
			jsonGen.writeStringField("expName", experienceName);
			jsonGen.writeBooleanField("isControl", isControl);
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw new RuntimeException("Yikes", e);
		}
		
		return body.toString();
	}
  
}
