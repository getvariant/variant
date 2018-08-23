package com.variant.core.impl;

import static com.variant.core.StateRequestStatus.Committed;
import static com.variant.core.StateRequestStatus.Failed;

import java.io.Serializable;
import java.util.Map;

import com.variant.core.StateRequestStatus;
import com.variant.core.TraceEvent;
import com.variant.core.schema.State;
import com.variant.core.util.immutable.ImmutableMap;


@SuppressWarnings("serial")
public class StateVisitedEvent extends TraceEventSupport implements Serializable {
		
	private final StateRequestStatus status;
	
	/**
	 */
	public StateVisitedEvent(State state, StateRequestStatus status) {
		super(TraceEvent.SVE_NAME);
		attributes.put("$STATE", state.getName());
		this.status = status;
		if (status != Committed && status != Failed) {
			throw new CoreException.Internal("Invalid status[" + status + "]");
		}
	}

	/**
	 */
	public StateVisitedEvent(State state, StateRequestStatus status, Map<String,String> attributes) {
		this(state, status);
		this.attributes.putAll(attributes);
	}

	/**
	 * Flushiers will need to get to the attribute map.
	 */
	public Map<String,String> getAttributes() {
		return new ImmutableMap<String,String>(attributes);
	}
	
	/**
	 */
	public StateRequestStatus getStatus() {
		return status;
	}
	
	/**
	 * Private instantiation, for desearialization, when state is not yet known.
	 *
	public StateVisitedEvent(CoreSession session) {
		super(session, EVENT_NAME);
	}
	*/
	/**
	 * Unmarschall from a JSON string.
	 * This now lives on the server. Client never unmarshals trace events.
	 *
	public static StateVisitedEvent fromJson(CoreSession session, String jsonStr) 
			throws JsonParseException, JsonMappingException, IOException {
		
		ObjectMapper mapper = new ObjectMapper();		
		@SuppressWarnings("unchecked")
		Map<String,?> mappedJson = mapper.readValue(jsonStr, Map.class);

		String sid = (String) mappedJson.get(FIELD_NAME_SID);
		if (!sid.equals(session.getId()))
			throw new CoreException.Internal(
					String.format("Session id [%s] does not match payload SID [%s]", session.getId(), sid));

		return TraceEventSupport.fromJson(new StateVisitedEvent(session), mappedJson);

	}
	*/
}
