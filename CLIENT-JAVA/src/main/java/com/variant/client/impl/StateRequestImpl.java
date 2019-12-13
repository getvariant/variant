package com.variant.client.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.VariantException;
import com.variant.client.util.MethodTimingWrapper;
import com.variant.client.TraceEvent;
import com.variant.share.error.ServerError;
import com.variant.share.schema.State;
import com.variant.share.schema.StateVariant;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;
import com.variant.share.session.CoreStateRequest;

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
	
	/**
	 * Both commit() and fail() call here.
	 * @param state
	 * @param userData
	 */
	private void _commit(StateRequest.Status status, Object... userData) {
		
		checkState();
		
		// If local state already reflects target state -- noop.
		if (status.ordinal() != coreRequest.getStatus().ordinal()) {
		
			// Persist targeting and session ID trackers.  Note that we expect the userData to apply to both.
			session.saveTrackers(userData);
			
			// Commit in shared state TODO: make this async
			conn.client.server.requestCommit(this, status);
			
			sve = null;
		}
	}

	public StateRequestImpl(SessionImpl session) 
	{	
		this.session = session;
		this.conn = (ConnectionImpl) session.getConnection();
		this.coreRequest = session.getCoreSession().getStateRequest().get();
		this.sve = new StateVisitedEvent(this.coreRequest.getState());
	}

	//---------------------------------------------------------------------------------------------//
	//                                            PUBLIC                                           //
	//---------------------------------------------------------------------------------------------//

	@Override
	public State getState() {
		checkState();
		return coreRequest.getState();
	}

	@Override
	public Optional<StateVariant> getResolvedStateVariant() {
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
	public Optional<Experience> getLiveExperience(Variation var) {
		checkState();
		return coreRequest.getLiveExperience(var);
	}

	@Override
	public void commit(Object... userData) {
		
		new MethodTimingWrapper<Object>().exec( () -> {
			if (getStatus() == StateRequest.Status.Failed)
				throw new VariantException(ServerError.CANNOT_COMMIT);
			else if (getStatus() == StateRequest.Status.InProgress)
				_commit(StateRequest.Status.Committed, userData);
			return null;  // void method -- making compiler happy.
		});
	}
	
	@Override
	public void fail(Object... userData) {

		new MethodTimingWrapper<Object>().exec( () -> {
			if (getStatus() == StateRequest.Status.Committed)
				throw new VariantException(ServerError.CANNOT_FAIL);
			else if (getStatus() == StateRequest.Status.InProgress)
				_commit(StateRequest.Status.Failed, userData);
			return null;  // void method -- making compiler happy.
		});
	}
	
	@Override
	public StateRequest.Status getStatus() {
		return StateRequest.Status.values()[coreRequest.getStatus().ordinal()];
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
