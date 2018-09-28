package com.variant.server.event;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.TraceEvent;
import com.variant.core.schema.Variation.Experience;
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

	private final String sessionId;
	private final ServerTraceEvent userEvent;
	private final Date timestamp = new Date();
	// Live experiences in effect at the time the event was triggered.
	private final Set<Experience> liveExperiences = new HashSet<Experience>();

	/**
	 * Constructor
	 * @return
	 */
	public FlushableTraceEventImpl(ServerTraceEvent event, Session session) {
		this.userEvent = event;
		this.sessionId = session.getId();
		Optional<StateRequest> reqOpt = session.getStateRequest();
		if (reqOpt.isPresent()) 
			for (Experience e: reqOpt.get().getLiveExperiences()) liveExperiences.add(e);
	
	}

	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public String getName() {
		return userEvent.getName();
	}

	@Override
	public String getSessionId() {
		return sessionId;
	}

	@Override
	public Map<String,String> getAttributes() {
		return userEvent.getAttributes();
	}
	
	@Override
	public Set<Experience> getLiveExperiences() {
		return liveExperiences;
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
