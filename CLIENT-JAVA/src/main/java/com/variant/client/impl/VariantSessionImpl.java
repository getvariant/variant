package com.variant.client.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantStateRequest;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.exception.Error;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.session.CoreSession;
import com.variant.core.session.CoreStateRequest;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.Tuples.Pair;
import com.variant.core.xdm.impl.StateImpl;

/**
 * Client side session implementation. Augments core session with client-side
 * functionality by wrapping it.
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
	private boolean committed = false;
	
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
			throw new VariantRuntimeUserErrorException(Error.RUN_SESSION_EXPIRED);
		}
	}

	// ---------------------------------------------------------------------------------------------//
	//                                       PUBLIC AUGMENTED                                       //
	// ---------------------------------------------------------------------------------------------//

	/**
	 *
	 */
	@Override
	public VariantStateRequest targetForState(State state) {
				
		checkState();
		
		// Can't have two requests at one time
		if (currentRequest != null && !currentRequest.isCommitted()) {
			throw new VariantRuntimeUserErrorException(RUN_ACTIVE_REQUEST);
		}
				
		return core.getRuntime().targetSessionForState(this, (StateImpl) state);
	}

	/**
	 * 
	 */
	public VariantSessionImpl(VariantClient client,
			CoreSession coreSession,
			VariantSessionIdTracker sessionIdTracker,
			VariantTargetingTracker targetingTracker) {
		
		this.client = (VariantClientImpl) client;
		this.coreSession = (CoreSession) coreSession;
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
	 * Commit this state request and trigger the state visited event.
	 * We don't actually need userData in this core implementation, but we want the method signature
	 * ready for the client.
	 */
	@Override
	public void commit() {

		session.checkState(); Used to check for enclosing session's non-expiration.
		
		if (isCommitted()) throw new IllegalStateException("Request already committed");
		
		// We won't have an event if nothing is instrumented on this state
		if (event != null) {
			// The status of this request.
			event.getParameterMap().put("REQ_STATUS", status);
			// log all resolved state params as event params.
			for (Map.Entry<String,String> e: resolvedParameterMap.entrySet()) {
				event.getParameterMap().put(e.getKey(), e.getValue());				
			}
			// Trigger state visited event
			// session.triggerEvent(event); -- on the client and server now.
			event = null;
		}
		
		committed = true;
		
		session.save();
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
