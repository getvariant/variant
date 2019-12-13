package com.variant.server.impl;

import java.io.Serializable;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.util.StringUtils;
import com.variant.server.api.FlushableTraceEvent;
import com.variant.server.api.StateRequest;
import com.variant.server.api.TraceEvent;
import com.variant.server.schema.ServerFlusherService;

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
	private static final Random rand = new Random();

	private final String id = StringUtils.random128BitString(rand);
	private final String sessionId;
	private final TraceEvent userEvent;
	private final Instant timestamp = Instant.now();
	// Live experiences in effect at the time the event was triggered.
	private final Set<Experience> liveExperiences = new HashSet<Experience>();
	private final ServerFlusherService flusherService;
	
	/**
	 * Constructor
	 * @return
	 */
	public FlushableTraceEventImpl(TraceEvent event, SessionImpl session) {
		this.userEvent =  event;
		this.sessionId = session.getId();
		Optional<StateRequest> reqOpt = session.getStateRequest();
		if (reqOpt.isPresent()) {
			for (Experience e: reqOpt.get().getLiveExperiences()) liveExperiences.add(e);
		}
		flusherService = session.getSchema().flusherService();
	}

	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public String getName() {
		return userEvent.getName();
	}

	@Override
	public String getId() {
		return id;
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
	@Override
	public Instant getTimestamp() {
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
	
	public ServerFlusherService getFlusherService() {
	   return flusherService;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String toString() {

	   DateTimeFormatter instantFormatter =
	         DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
	                          .withLocale( Locale.US )
	                          .withZone( ZoneId.systemDefault() );
	   
		final StringWriter result = new StringWriter(1024);

		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("createdOn", instantFormatter.format(timestamp));
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
