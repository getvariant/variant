package com.variant.client.impl;

import java.util.Collection;
import java.util.Set;

import com.variant.client.VariantSession;
import com.variant.client.VariantStateRequest;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.CoreStateRequestImpl;
import com.variant.core.xdm.State;
import com.variant.core.xdm.StateVariant;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;

public class VariantStateRequestImpl implements VariantStateRequest {

	private VariantSessionImpl session;
	private CoreStateRequestImpl coreStateRequest;
	
	public VariantStateRequestImpl(
			VariantCoreStateRequest coreStateRequest, 
			VariantSessionImpl clientSession) 
	{	
		this.coreStateRequest = (CoreStateRequestImpl) coreStateRequest;
		this.session = (VariantSessionImpl) clientSession;
	}

	//---------------------------------------------------------------------------------------------//
	//                                     PUBLIC PASS-THRU                                        //
	//---------------------------------------------------------------------------------------------//
	@Override
	public State getState() {
		return coreStateRequest.getState();
	}

	@Override
	public StateVariant getResolvedStateVariant() {
		return coreStateRequest.getResolvedStateVariant();
	}

	@Override
	public String getResolvedParameter(String name) {
		return coreStateRequest.getResolvedParameter(name);
	}

	@Override
	public Set<String> getResolvedParameterNames() {
		return coreStateRequest.getResolvedParameterNames();
	}

	@Override
	public Collection<Experience> getLiveExperiences() {
		return coreStateRequest.getLiveExperiences();
	}

	@Override
	public Experience getLiveExperience(Test test) {
		return coreStateRequest.getLiveExperience(test);
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
	public void commit() {
		throw new VariantRuntimeException("Method not supported. Use commit(Object...) instead.");
	}
	
	@Override
	public void commit(Object... userData) {
		
		coreStateRequest.commit();
		
		// Trigger state visited event
		session.triggerEvent(event);

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
	/**
	 * Override with a narrower return type to return the client session, instead of core.
	 */
	@Override
	public VariantSession getSession() {
		return session;
	}

	public CoreStateRequestImpl getCoreStateRequest () {
		return coreStateRequest;
	}

}
