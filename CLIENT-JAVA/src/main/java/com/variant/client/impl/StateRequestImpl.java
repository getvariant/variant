package com.variant.client.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.variant.client.Session;
import com.variant.client.StateNotInstrumentedException;
import com.variant.client.StateRequest;
import com.variant.core.CoreException;
import com.variant.core.ServerError;
import com.variant.core.StateRequestStatus;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreStateRequest;

/**
 * Permanent wrapper around transient CoreStateRequest + client only request functionality.
 */
public class StateRequestImpl implements StateRequest {

	private final SessionImpl session;
	private final ConnectionImpl conn;
	private CoreStateRequest coreRequest;
	
	private void checkState() {
		session.checkState();
	}
	
	public StateRequestImpl(SessionImpl session) 
	{	
		this.session = session;
		this.conn = (ConnectionImpl) session.getConnection();
		this.coreRequest = session.getCoreSession().getStateRequest();
	}

	//---------------------------------------------------------------------------------------------//
	//                                            PUBLIC                                           //
	//---------------------------------------------------------------------------------------------//
	@Override
	public Date getCreateDate() {
		return coreRequest.createDate();
	}

	@Override
	public State getState() {
		checkState();
		return session.getCoreSession().getStateRequest().getState();
	}

	@Override
	public StateVariant getResolvedStateVariant() {
		checkState();
		return session.getCoreSession().getStateRequest().getResolvedStateVariant();
	}

	@Override
	public Map<String,String> getResolvedParameters() {
		checkState();
		return session.getCoreSession().getStateRequest().getResolvedParameters();
	}

	@Override
	public Set<Experience> getLiveExperiences() {
		checkState();
		return session.getCoreSession().getStateRequest().getLiveExperiences();
	}

	@Override
	public Experience getLiveExperience(Test test) {
		checkState();
		try {
			return session.getCoreSession().getStateRequest().getLiveExperience(test);
		}
		catch (CoreException.User e) {
			if (e.error.getCode() == ServerError.STATE_NOT_INSTRUMENTED_BY_TEST.getCode())
				throw new StateNotInstrumentedException(e);
			else throw e;
		}
	}

	@Override
	public VariantEvent getStateVisitedEvent() {
		checkState();
		return session.getCoreSession().getStateRequest().getStateVisitedEvent();
	}

	@Override
	public boolean commit(Object... userData) {
		
		checkState();
		if (isCommitted()) return false;
		
		// Persist targeting and session ID trackers.  Note that we expect the userData to apply to both.
		session.saveTrackers(userData);
		
		return conn.client.server.requestCommit(session, conn);
	}

	@Override
	public boolean isCommitted() {
		return session.getCoreSession().getStateRequest().isCommitted();
	}

	@Override
	public void setStatus(StateRequestStatus status) {
		session.getCoreSession().getStateRequest().setStatus(status);
	}

	@Override
	public StateRequestStatus getStatus() {
		return session.getCoreSession().getStateRequest().getStatus();
	}
	
	/**
	 * Override with a narrower return type to return the client session, instead of core.
	 */
	@Override
	public Session getSession() {
		return session;
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//
	
	public void rewrap(CoreStateRequest coreRequest) {
		this.coreRequest = coreRequest;
	}
	
	public CoreStateRequest getCoreStateRequest() {
		return coreRequest;
	}
	
}
