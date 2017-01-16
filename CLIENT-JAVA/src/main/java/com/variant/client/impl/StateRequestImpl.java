package com.variant.client.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.variant.client.Session;
import com.variant.client.StateNotInstrumentedException;
import com.variant.client.StateRequest;
import com.variant.core.StateRequestStatus;
import com.variant.core.VariantEvent;
import com.variant.core.exception.CommonError;
import com.variant.core.exception.CoreException;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreStateRequest;

public class StateRequestImpl implements StateRequest {

	private SessionImpl session;
	private CoreStateRequest coreRequest;

	private void checkState() {
		session.checkState();
	}
	
	public StateRequestImpl(CoreStateRequest coreStateRequest, SessionImpl clientSession) 
	{	
		this.coreRequest = (CoreStateRequest) coreStateRequest;
		this.session = (SessionImpl) clientSession;
	}

	//---------------------------------------------------------------------------------------------//
	//                                            PUBLIC                                           //
	//---------------------------------------------------------------------------------------------//
	@Override
	public Date createDate() {
		return null;
	}

	@Override
	public State getState() {
		checkState();
		return coreRequest.getState();
	}

	@Override
	public StateVariant getResolvedStateVariant() {
		checkState();
		return coreRequest.getResolvedStateVariant();
	}

	@Override
	public Map<String,String> getResolvedParameters() {
		checkState();
		return coreRequest.getResolvedParameters();
	}

	@Override
	public Set<Experience> getLiveExperiences() {
		checkState();
		return coreRequest.getLiveExperiences();
	}

	@Override
	public Experience getLiveExperience(Test test) {
		checkState();
		try {
			return coreRequest.getLiveExperience(test);
		}
		catch (CoreException.User e) {
			if (e.error.code == CommonError.STATE_NOT_INSTRUMENTED_BY_TEST.code)
				throw new StateNotInstrumentedException(e);
			else throw e;
		}
	}

	@Override
	public VariantEvent getStateVisitedEvent() {
		checkState();
		return coreRequest.getStateVisitedEvent();
	}

	@Override
	public boolean commit(Object... userData) {
		
		checkState();
		if (coreRequest.isCommitted()) return false;
		
		// Persist targeting and session ID trackers.  Note that we expect the userData to apply to both.
		session.getTargetingTracker().save(userData);
		session.getSessionIdTracker().save(userData);
				
		coreRequest.commit();
		
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
