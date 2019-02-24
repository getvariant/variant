package com.variant.client.impl;

import static com.variant.core.StateRequestStatus.Committed;
import static com.variant.core.StateRequestStatus.Failed;

import java.io.Serializable;
import java.util.Map;

import com.variant.core.StateRequestStatus;
import com.variant.core.error.CoreException;
import com.variant.core.schema.State;
import com.variant.core.util.immutable.ImmutableMap;

@SuppressWarnings("serial")
public class StateVisitedEvent extends TraceEventSupport implements Serializable {
		
	public static final String SVE_NAME = "$STATE_VISITED";

	private final StateRequestStatus status;
	
	/**
	 */
	public StateVisitedEvent(State state) {
		super(SVE_NAME);
		attributes.put("$STATE", state.getName());
		this.status = StateRequestStatus.InProgress;
	}

	/**
	 */
	public StateVisitedEvent(State state, StateRequestStatus status) {
		super(SVE_NAME);
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
	
}
