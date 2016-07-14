package com.variant.client.impl;

import java.util.Collection;

import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantStateRequest;
import com.variant.client.VariantTargetingTracker;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.CoreStateRequestImpl;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.Tuples.Pair;

/**
 * Client side session implementation.
 * Augments core session with client-side functionality by wrapping it.
 * 
 * @author Igor
 *
 */
public class VariantSessionImpl implements VariantSession {

	
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;
	
	private CoreSessionImpl coreSession;
	private VariantSessionIdTracker sessionIdTracker;
	private VariantTargetingTracker targetingTracker;
	
	private static SessionScopedTargetingStabile toTargetingStable(VariantTargetingTracker tt) {
	
		SessionScopedTargetingStabile result = new SessionScopedTargetingStabile();
		for (VariantTargetingTracker.Entry e: tt.get()) 
			result.add(e.getExperience(), e.getTimestamp());
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	VariantSessionIdTracker getSessionIdTracker() {
		return sessionIdTracker;
	}
	
	/**
	 * 
	 * @return
	 */
	VariantTargetingTracker getTargetingTracker() {
		return targetingTracker;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                     PUBLIC AUGMENTED                                        //
	//---------------------------------------------------------------------------------------------//

	public VariantSessionImpl(VariantCoreSession coreSession, VariantSessionIdTracker sessionIdTracker, VariantTargetingTracker targetingTracker) {
		this.coreSession = (CoreSessionImpl) coreSession;
		this.coreSession.setTargetingStabile(toTargetingStable(targetingTracker));
		this.sessionIdTracker = sessionIdTracker;
		this.targetingTracker = targetingTracker;
	}

	
	@Override
	public VariantStateRequest targetForState(State state) {

		CoreStateRequestImpl coreReq = (CoreStateRequestImpl) coreSession.targetForState(state);
		
		return new VariantStateRequestImpl(coreReq, this);
	}

	//---------------------------------------------------------------------------------------------//
	//                                     PUBLIC PASS-THRU                                        //
	//---------------------------------------------------------------------------------------------//

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
	public Collection<Test> getTraversedTests() {
		return coreSession.getTraversedTests();
	}

	@Override
	public Collection<Test> getDisqualifiedTests() {
		return coreSession.getDisqualifiedTests();
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		coreSession.triggerEvent(event);
	}

	@Override
	public VariantCoreStateRequest getStateRequest() {
		return coreSession.getStateRequest();
	}

}
