package com.variant.core.impl;

import java.io.Serializable;
import java.util.Map;

import com.variant.core.exception.CoreException;
import com.variant.core.schema.State;
import com.variant.core.session.CoreSession;


@SuppressWarnings("serial")
public class StateVisitedEvent extends VariantEventSupport implements Serializable {

	public static final String EVENT_NAME = "$STATE_VISIT";
		
	/**
	 * Public instantiation
	 */
	public StateVisitedEvent(CoreSession session, State state) {
		super(session, EVENT_NAME);
		value = state.getName();
	}
	
	/**
	 * Private instantiation, for desearialization, when state is not yet known.
	 */
	public StateVisitedEvent(CoreSession session) {
		super(session, EVENT_NAME);
	}
	
	public static StateVisitedEvent fromJson(CoreSession session, Map<String,?> mappedJson) {
		
		String sid = (String) mappedJson.get(FIELD_NAME_SID);
		if (!sid.equals(session.getId()))
			throw new CoreException.Internal(
					String.format("Session id [%s] does not match payload SID [%s]", session.getId(), sid));

		return VariantEventSupport.fromJson(new StateVisitedEvent(session), mappedJson);

	}
}
