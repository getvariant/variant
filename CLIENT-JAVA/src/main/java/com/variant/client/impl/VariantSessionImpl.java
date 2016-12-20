package com.variant.client.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantStateRequest;
import com.variant.client.VariantTargetingTracker;

import static com.variant.client.impl.ClientError.*;

import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.VariantEvent;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.impl.StateImpl;
import com.variant.core.session.CoreSession;
import com.variant.core.session.CoreStateRequest;
import com.variant.core.session.SessionScopedTargetingStabile;

/**
 * Variant session as visible to the client code. 
 * 
 * @author Igor
 *
 */
public class VariantSessionImpl implements VariantSession {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private boolean isExpired = false;
	
	private VariantClientImpl client;
	private CoreSession coreSession;
	private VariantSessionIdTracker sessionIdTracker;
	private VariantTargetingTracker targetingTracker;
	private HashMap<String, Object> attributeMap = new HashMap<String, Object>();
	
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
				result.add(e.getAsExperience(coreSession.getSchema()), e.getTimestamp());
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
	 * Make sure session has not expired.
	 * We should not throw exception here. Perhaps OPTINAL
	 */
	public void checkState() {
//		if (isExpired) {
//			throw new VariantRuntimeUserErrorException(Error.RUN_SESSION_EXPIRED);
//		}
	}

	// ---------------------------------------------------------------------------------------------//
	//                                       PUBLIC AUGMENTED                                       //
	// ---------------------------------------------------------------------------------------------//

	/**
	 * 
	 */
	public VariantSessionImpl(VariantClient client,
			CoreSession coreSession,
			VariantSessionIdTracker sessionIdTracker,
			VariantTargetingTracker targetingTracker) {
		
		this.client = (VariantClientImpl) client;
		this.coreSession = coreSession;
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
		
		// Can't have two requests at one time
		if (coreSession.getStateRequest() != null && !coreSession.getStateRequest().isCommitted()) {
			throw new ClientErrorException(ACTIVE_REQUEST);
		}
				
		return core.getRuntime().targetSessionForState(this, (StateImpl) state);

		CoreStateRequest coreReq = (CoreStateRequest) coreSession.targetForState(state);
		targetingTracker.set(fromTargetingStabile(coreSession.getTargetingStabile()));
		return new VariantStateRequestImpl(coreReq, this);
	}

	/**
	 * 
	 */
	@Override
	public boolean isExpired() {
		return isExpired;
	}
	
	@Override
	public Object setAttribute(String name, Object value) {
		return attributeMap.put(name, value);
	}

	@Override
	public Object getAttribute(String name) {
		return attributeMap.get(name);
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
	public Connection getConnectoin() {
		checkState();
		return coreSession.getSchema();
	}

	@Override
	public Map<State, Integer> getTraversedStates() {
		checkState();
		return coreSession.getTraversedStates();
	}

	@Override
	public Set<Test> getTraversedTests() {
		checkState();
		return coreSession.getTraversedTests();
	}

	@Override
	public Set<Test> getDisqualifiedTests() {
		checkState();
		return coreSession.getDisqualifiedTests();
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		checkState();
		// Trigger state visited event
		session.triggerEvent(event);

		coreSession.triggerEvent(event);
	}

	@Override
	public VariantCoreStateRequest getStateRequest() {
		checkState();
		VariantCoreStateRequest coreRequest = coreSession.getStateRequest();
		return coreRequest == null ? null : new VariantStateRequestImpl(coreRequest, this);
	}

	@Override
	public VariantClient getClient() {
		return client;
	}

	/**
	 */
	@Override
	public boolean isCommitted() {
		return committed;
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
	public void replaceCoreSession(VariantCoreSession coreSession) {
		this.coreSession = (CoreSession) coreSession;
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
