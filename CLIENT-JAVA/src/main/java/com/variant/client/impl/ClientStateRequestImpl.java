package com.variant.client.impl;

import java.util.Collection;
import java.util.Map;

import com.variant.client.VariantSession;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.impl.CoreStateRequestImpl;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class ClientStateRequestImpl implements VariantStateRequest {

	private VariantSessionImpl session;
	private CoreStateRequestImpl coreStateRequest;
	
	public ClientStateRequestImpl(VariantStateRequest coreStateRequest, VariantSession session) {
		this.coreStateRequest = (CoreStateRequestImpl) coreStateRequest;
		this.session = (VariantSessionImpl) session;
	}

	//---------------------------------------------------------------------------------------------//
	//                                     PUBLIC PASS-THRU                                        //
	//---------------------------------------------------------------------------------------------//
	@Override
	public VariantCoreSession getSession() {
		return coreStateRequest.getSession();
	}

	@Override
	public State getState() {
		return coreStateRequest.getState();
	}

	@Override
	public Map<String, String> getResolvedParameterMap() {
		return coreStateRequest.getResolvedParameterMap();
	}

	@Override
	public Collection<Experience> getActiveExperiences() {
		return coreStateRequest.getActiveExperiences();
	}

	@Override
	public Experience getActiveExperience(Test test) {
		return coreStateRequest.getActiveExperience(test);
	}

	@Override
	public VariantEvent getStateVisitedEvent() {
		return coreStateRequest.getStateVisitedEvent();
	}

	@Override
	public void setStatus(Status status) {
		coreStateRequest.setStatus(status);
	}
	
	@Override
	public boolean isCommitted() {
		return coreStateRequest.isCommitted();
	}

	@Override
	public void commit(Object... userData) {
		
		coreStateRequest.commit();
		
		// Persist targeting and session ID trackers.  Note that we expect the userData to apply to both.
		session.getTargetingTracker().save(userData);
		session.getSessionIdTracker().save(userData);
	}

	@Override
	public Status getStatus() {
		return coreStateRequest.getStatus();
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC EXT                                         //
	//---------------------------------------------------------------------------------------------//

	public CoreStateRequestImpl getCoreStateRequest () {
		return coreStateRequest;
	}
	
	/**
	 * Plug the no argument invocation and channel it via the varargs.
	 * TODO In 8, this should move to the interface.
	 */
	public void commit() {
		commit((Object[])null);
	}

}
