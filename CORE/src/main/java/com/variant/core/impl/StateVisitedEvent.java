package com.variant.core.impl;

import java.io.Serializable;
import java.util.Map;

import com.variant.core.schema.State;
import com.variant.core.session.CoreSession;
import com.variant.core.util.immutable.ImmutableMap;


@SuppressWarnings("serial")
public class StateVisitedEvent extends TraceEventSupport implements Serializable {

	public static final String EVENT_NAME = "$STATE_VISIT";
		
	/**
	 */
	public StateVisitedEvent(CoreSession session, State state) {
		super(session, EVENT_NAME);
		value = state.getName();
	}

	/**
	 */
	public StateVisitedEvent(CoreSession session, State state, Map<String,String> attributes) {
		this(session, state);
		this.attributes = attributes;
	}

	/**
	 * Flushiers will need to get to the attribute map.
	 * @return
	 */
	public Map<String,String> getAttributes() {
		return new ImmutableMap<String,String>(attributes);
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
