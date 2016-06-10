package com.variant.client.impl;

import java.util.Collection;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

/**
 * 
 */
public class VariantClientSession implements VariantSession {

	private com.variant.core.VariantSession coreSession;
	
	public VariantClientSession(VariantSession coreSession) {
		this.coreSession = coreSession;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/// Overridden methods ///
	@Override
	public VariantStateRequest targetForState(State state, Object... targetingTrackerUserData) {
		VariantStateRequest result = coreSession.targetForState(state, targetingTrackerUserData);
		return new ClientStateRequestWrapper(this, result, targetingTrackerUserData);
	}
	
	/// Pass-through methods ///
	@Override
	public String getId() {
		return coreSession.getId();
	}

	@Override
	public long creationTimestamp() {
		return coreSession.creationTimestamp();
	}

	@Override
	public String getSchemaId() {
		return coreSession.getSchemaId();
	}

	@Override
	public Collection<Pair<State, Integer>> getTraversedStates() {
		return coreSession.getTraversedStates();
	}

	@Override
	public Collection<Pair<Test, Boolean>> getTraversedTests() {
		return coreSession.getTraversedTests();
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		coreSession.triggerEvent(event);
	}

	@Override
	public VariantStateRequest getStateRequest() {
		return coreSession.getStateRequest();
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	// Make core session available to tests.
	public VariantSession getCoreSession() {
		return coreSession;
	}

}
