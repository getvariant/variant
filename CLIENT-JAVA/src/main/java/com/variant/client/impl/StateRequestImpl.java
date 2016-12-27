package com.variant.client.impl;

import java.util.Map;
import java.util.Set;

import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.core.StateRequestStatus;
import com.variant.core.VariantEvent;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreStateRequest;

public class StateRequestImpl implements StateRequest {

	private SessionImpl session;
	private CoreStateRequest coreRequest;

	public StateRequestImpl(CoreStateRequest coreStateRequest, SessionImpl clientSession) 
	{	
		this.coreRequest = (CoreStateRequest) coreStateRequest;
		this.session = (SessionImpl) clientSession;
	}

	//---------------------------------------------------------------------------------------------//
	//                                     PUBLIC PASS-THRU                                        //
	//---------------------------------------------------------------------------------------------//
	@Override
	public State getState() {
		return coreRequest.getState();
	}

	@Override
	public StateVariant getResolvedStateVariant() {
		return coreRequest.getResolvedStateVariant();
	}

	@Override
	public Map<String,String> getResolvedParameters() {
		return coreRequest.getResolvedParameters();
	}

	@Override
	public Set<Experience> getLiveExperiences() {
		return coreRequest.getLiveExperiences();
	}

	@Override
	public Experience getLiveExperience(Test test) {
		return coreRequest.getLiveExperience(test);
	}

	@Override
	public VariantEvent getStateVisitedEvent() {
		return coreRequest.getStateVisitedEvent();
	}

	@Override
	public boolean commit(Object... userData) {
		
		if (coreRequest.isCommitted()) return false;

		session.checkState(); // Used to check for enclosing session's non-expiration.
		
		// Persist targeting and session ID trackers.  Note that we expect the userData to apply to both.
		session.getTargetingTracker().save(userData);
		session.getSessionIdTracker().save(userData);
		
		
		// Trigger state visited event
		VariantEvent event = coreRequest.getStateVisitedEvent();

		// We won't have an event if nothing is instrumented on this state
		if (event != null) {
			// The status of this request.
			event.getParameterMap().put("$REQ_STATUS", coreRequest.getStatus().name());
			// log all resolved state params as event params.
			for (Map.Entry<String,String> e: coreRequest.getResolvedParameters().entrySet()) {
				event.getParameterMap().put(e.getKey(), e.getValue());				
			}
			// Trigger state visited event
			session.triggerEvent(event);
			event = null;
		}
		
		coreRequest.commit();
		session.save();
		
		return true;
	}

	@Override
	public boolean isCommitted() {
		return coreRequest.isCommitted();
	}

	@Override
	public void setStatus(StateRequestStatus status) {
		coreRequest.setStatus(status);
	}

	@Override
	public StateRequestStatus getStatus() {
		return coreRequest.getStatus();
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC EXT                                         //
	//---------------------------------------------------------------------------------------------//
	/**
	 * Override with a narrower return type to return the client session, instead of core.
	 */
	@Override
	public Session getSession() {
		return session;
	}

	/*
	public CoreStateRequest getCoreStateRequest () {
		return coreRequest;
	}
*/
}
