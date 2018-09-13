package com.variant.client.impl;

import java.util.Map;
import java.util.Set;

import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantException;
import com.variant.core.StateRequestStatus;
import com.variant.core.TraceEvent;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.StateVisitedEvent;
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
	private StateVisitedEvent sve;
	
	/**
	 * Ok to use this object?
	 */
	private void checkState() {
		session.preChecks();
	}
	
	public StateRequestImpl(SessionImpl session) 
	{	
		this.session = session;
		this.conn = (ConnectionImpl) session.getConnection();
		this.coreRequest = session.getCoreSession().getStateRequest();
		this.sve = new StateVisitedEvent(this.coreRequest.getState());
	}

	//---------------------------------------------------------------------------------------------//
	//                                            PUBLIC                                           //
	//---------------------------------------------------------------------------------------------//

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
		return session.getCoreSession().getStateRequest().getLiveExperience(test);
	}

	/**
	 * Both commit() and fail() call here.
	 * @param state
	 * @param userData
	 */
	public void _commit(StateRequestStatus status, Object... userData) {
		
		checkState();
		
		// If local state already reflects target state -- noop.
		if (status != session.getCoreSession().getStateRequest().getStatus()) {
		
			// Persist targeting and session ID trackers.  Note that we expect the userData to apply to both.
			session.saveTrackers(userData);
			
			// Commit in shared state TODO: make this async
			conn.client.server.requestCommit(this, status);
			
			sve = null;
		}
	}

	@Override
	public void commit(Object... userData) {
		
		if (getStatus() == StateRequestStatus.Failed)
			throw new VariantException(ServerError.CANNOT_COMMIT);
		else if (getStatus() == StateRequestStatus.InProgress)
			_commit(StateRequestStatus.Committed, userData);
	}
	
	@Override
	public void fail(Object... userData) {
		if (getStatus() == StateRequestStatus.Committed)
			throw new VariantException(ServerError.CANNOT_FAIL);
		else if (getStatus() == StateRequestStatus.InProgress)
		_commit(StateRequestStatus.Failed, userData);
	}
	
	@Override
	public StateRequestStatus getStatus() {
		return session.getCoreSession().getStateRequest().getStatus();
	}

	@Override
	public TraceEvent getStateVisitedEvent() {
		return sve;
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

}
