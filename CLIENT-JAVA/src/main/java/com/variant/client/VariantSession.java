package com.variant.client;

import java.util.Collection;

import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

/**
 */
public class VariantSession implements com.variant.core.VariantSession {

	private com.variant.core.VariantSession coreSession;
	
	public VariantSession(com.variant.core.VariantSession coreSession) {
		this.coreSession = coreSession;
	}

	// Make core session available to tests.
	com.variant.core.VariantSession getCoreSession() {
		return coreSession;
	}
	
	/// Overridden methods ///
	@Override
	public VariantStateRequest targetForState(State state, Object... targetingTrackerUserData) {
		VariantStateRequest result = coreSession.targetForState(state, targetingTrackerUserData);
		return new ClientStateRequestWrapper(this, result, targetingTrackerUserData);
	}
	
	/// Pass-through methods ///
	public String getId() {
		return coreSession.getId();
	}

	public long creationTimestamp() {
		return coreSession.creationTimestamp();
	}

	public String getSchemaId() {
		return coreSession.getSchemaId();
	}

	public Collection<Pair<State, Integer>> getTraversedStates() {
		return coreSession.getTraversedStates();
	}

	public Collection<Pair<Test, Boolean>> getTraversedTests() {
		return coreSession.getTraversedTests();
	}

	public void triggerEvent(VariantEvent event) {
		coreSession.triggerEvent(event);
	}

	public VariantStateRequest getStateRequest() {
		return coreSession.getStateRequest();
	}

}
