package com.variant.client.session;

import java.util.ArrayList;
import java.util.Collection;

import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.impl.ClientStateRequestImpl;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.CoreStateRequestImpl;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.Tuples.Pair;

/**
 * Client side session implementation. Augments core session with client-side
 * functionality by wrapping it.
 * 
 * @author Igor
 *
 */
public class ClientSessionImpl implements VariantSession {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private boolean isExpired = false;
	
	private CoreSessionImpl coreSession;
	private VariantSessionIdTracker sessionIdTracker;
	private VariantTargetingTracker targetingTracker;

	/**
	 * 
	 * @param tt
	 * @return
	 */
	private SessionScopedTargetingStabile toTargetingStabile(VariantTargetingTracker tt) {

		SessionScopedTargetingStabile result = new SessionScopedTargetingStabile();
		Collection<VariantTargetingTracker.Entry> entries = tt.get();
		if (entries != null)
			for (VariantTargetingTracker.Entry e : entries)
				result.add(e.getAsExperience(coreSession.getCoreApi().getSchema()), e.getTimestamp());
		return result;
	}

	/**
	 * 
	 * @param stabile
	 * @return
	 */
	private Collection<VariantTargetingTracker.Entry> fromTargetingStabile(SessionScopedTargetingStabile stabile) {
		ArrayList<VariantTargetingTracker.Entry> result = new ArrayList<VariantTargetingTracker.Entry>(stabile.size());
		for (SessionScopedTargetingStabile.Entry stabileEntry: stabile.getAll()) 
			result.add(new TargetingTrackerEntryImpl(stabileEntry));
		return result;
	}
	
	/**
	 * 
	 */
	private void checkState() {
		if (isExpired) {
			throw new VariantRuntimeUserErrorException(MessageTemplate.RUN_SESSION_EXPIRED);
		}
	}

	// ---------------------------------------------------------------------------------------------//
	//                                       PUBLIC AUGMENTED                                       //
	// ---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	public ClientSessionImpl(CoreSessionImpl coreSession,
			VariantSessionIdTracker sessionIdTracker,
			VariantTargetingTracker targetingTracker) {
		
		this.coreSession = (CoreSessionImpl) coreSession;
		this.coreSession.setTargetingStabile(toTargetingStabile(targetingTracker));
		this.sessionIdTracker = sessionIdTracker;
		this.targetingTracker = targetingTracker;
	}

	/**
	 * 
	 */
	@Override
	public VariantStateRequest targetForState(State state) {
		checkState();
		CoreStateRequestImpl coreReq = (CoreStateRequestImpl) coreSession.targetForState(state);
		targetingTracker.set(fromTargetingStabile(coreSession.getTargetingStabile()));
		return new ClientStateRequestImpl(coreReq, this);
	}

	/**
	 * 
	 */
	@Override
	public boolean isExpired() {
		return isExpired;
	}
	
	// ---------------------------------------------------------------------------------------------//
	//                                      PUBLIC PASS-THRU                                        //
	// ---------------------------------------------------------------------------------------------//

	@Override
	public String getId() {
		checkState();
		return coreSession.getId();
	}

	@Override
	public long creationTimestamp() {
		checkState();
		return coreSession.creationTimestamp();
	}

	@Override
	public String getSchemaId() {
		checkState();
		return coreSession.getSchemaId();
	}

	@Override
	public Collection<Pair<State, Integer>> getTraversedStates() {
		checkState();
		return coreSession.getTraversedStates();
	}

	@Override
	public Collection<Test> getTraversedTests() {
		checkState();
		return coreSession.getTraversedTests();
	}

	@Override
	public Collection<Test> getDisqualifiedTests() {
		checkState();
		return coreSession.getDisqualifiedTests();
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		checkState();
		coreSession.triggerEvent(event);
	}

	@Override
	public VariantStateRequest getStateRequest() {
		checkState();
		VariantStateRequest coreRequest = coreSession.getStateRequest();
		return coreRequest == null ? null : new ClientStateRequestImpl(coreRequest, this);
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @return
	 */
	public VariantSessionIdTracker getSessionIdTracker() {
		return sessionIdTracker;
	}

	/**
	 * 
	 * @return
	 */
	public VariantTargetingTracker getTargetingTracker() {
		return targetingTracker;
	}

	/**
	 * 
	 * @param coreSession
	 */
	public void replaceCoreSession(VariantSession coreSession) {
		this.coreSession = (CoreSessionImpl) coreSession;
	}
	
	/**
	 * Expire this session object.
	 */
	public void expire() {
		isExpired = true;
		coreSession = null;
		sessionIdTracker = null;
		targetingTracker = null;
	}
}
