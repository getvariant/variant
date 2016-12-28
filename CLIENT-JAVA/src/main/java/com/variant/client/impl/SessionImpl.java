package com.variant.client.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.Session;
import com.variant.client.SessionIdTracker;
import com.variant.client.StateRequest;
import com.variant.client.TargetingTracker;

import static com.variant.client.impl.ClientError.*;

import com.variant.client.conn.ConnectionImpl;
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
public class SessionImpl implements Session {

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	private boolean isExpired = false;
	
	private final ConnectionImpl conn;
	private CoreSession coreSession;
	private SessionIdTracker sessionIdTracker;
	private TargetingTracker targetingTracker;
	private HashMap<String, Object> attributeMap = new HashMap<String, Object>();
	
	/**
	 * 
	 * @param tt
	 * @return
	 */
	private SessionScopedTargetingStabile toTargetingStabile(TargetingTracker tt) {

		SessionScopedTargetingStabile result = new SessionScopedTargetingStabile();
		Collection<TargetingTracker.Entry> entries = tt.get();
		if (entries != null)
			for (TargetingTracker.Entry e : entries)
				result.add(e.getAsExperience(coreSession.getSchema()), e.getTimestamp());
		return result;
	}

	/**
	 * 
	 * @param stabile
	 * @return
	 */
	private Collection<TargetingTracker.Entry> fromTargetingStabile(SessionScopedTargetingStabile stabile) {
		ArrayList<TargetingTracker.Entry> result = new ArrayList<TargetingTracker.Entry>(stabile.size());
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
	public SessionImpl(Connection conn,
			CoreSession coreSession,
			SessionIdTracker sessionIdTracker,
			TargetingTracker targetingTracker) {
		
		this.conn = (ConnectionImpl) conn;
		this.coreSession = coreSession;
		this.coreSession.setTargetingStabile(toTargetingStabile(targetingTracker));
		this.sessionIdTracker = sessionIdTracker;
		this.targetingTracker = targetingTracker;
	}

	/**
	 *
	 *
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
		return conn;
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
		conn.getServer().saveEvent(this, event);
	}
/*
	@Override
	public VariantStateRequest getStateRequest() {
		checkState();
		VariantCoreStateRequest coreRequest = coreSession.getStateRequest();
		return coreRequest == null ? null : new VariantStateRequestImpl(coreRequest, this);
	}
*/
	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	public void save() {
		conn.getServer().saveSession(conn, coreSession);
	}
	
	/**
	 * 
	 * @return
	 */
	public SessionIdTracker getSessionIdTracker() {
		return sessionIdTracker;
	}

	/**
	 * 
	 * @return
	 */
	public TargetingTracker getTargetingTracker() {
		return targetingTracker;
	}

	/**
	 * 
	 * @param coreSession
	 */
	public void replaceCoreSession(CoreSession coreSession) {
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