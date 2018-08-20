package com.variant.server.event;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.TraceEvent;
import com.variant.core.schema.Test.Experience;
import com.variant.server.api.FlushableTraceEvent;
import com.variant.server.api.Session;
import com.variant.server.api.StateRequest;

/**
 * Flushable event implementation suitable for the server.
 * 
 * @author Igor.
 *
 */
public class FlushableTraceEventImpl implements FlushableTraceEvent, Serializable {
			
	/**
	 */
	private static final long serialVersionUID = 1L;

	private Session session;
	private ServerTraceEvent userEvent;
	private Date timestamp = new Date();
	
	/**
	 * Constructor
	 * @return
	 */
	public FlushableTraceEventImpl(ServerTraceEvent event, Session session) {
		this.userEvent = event;		
		this.session = session;
	}

	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public String getName() {
		return userEvent.getName();
	}

	@Override
	public Map<String,String> getAttributes() {
		return userEvent.getAttributes();
	}
	
	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public Set<Experience> getLiveExperiences() {
		
		Set<Experience> result = new HashSet<Experience>();
		StateRequest req = session.getStateRequest();
		if (req != null) 
			for (Experience e: req.getLiveExperiences()) result.add(e);
		return result;
	}

	/**
	 * Add creation ts.
	 */
	public Date getCreateDate() {
		return timestamp;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//	
	/**
	 * The event we're wrapping.
	 * @return
	 */
	public TraceEvent getOriginalEvent() {
		return userEvent;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String toString() {

		final StringWriter result = new StringWriter(1024);

		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("sid", session.getId());
			jsonGen.writeNumberField("createdOn", getCreateDate().getTime());
			jsonGen.writeStringField("name", getName());
			jsonGen.writeObjectFieldStart("attrs");
			for (Map.Entry<String, String> attr: getAttributes().entrySet()) {
				jsonGen.writeStringField(attr.getKey(), attr.getValue());
			}
			jsonGen.writeEndObject();
			
			jsonGen.writeArrayFieldStart("expList");
			for (Experience e: getLiveExperiences()) {
				jsonGen.writeObject(e.toString());
			}
			jsonGen.writeEndArray();
				
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw new RuntimeException("Yikes", e);
		}
		return result.toString();

	}

}
