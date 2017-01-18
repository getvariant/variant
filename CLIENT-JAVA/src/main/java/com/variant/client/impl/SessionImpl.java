package com.variant.client.impl;

import static com.variant.client.ClientUserError.ACTIVE_REQUEST;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.StateRequest;
import com.variant.client.TargetingTracker;
import com.variant.client.conn.ConnectionImpl;
import com.variant.client.net.Payload;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
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

	private boolean isExpired = false;
	
	private final ConnectionImpl conn;  // Connection which created this session.
	private CoreSession coreSession;
	private SessionIdTracker sessionIdTracker;
	private TargetingTracker targetingTracker;
	// client-local attributes. They do not get replicated to the server.
	private HashMap<String, Object> attributeMap = new HashMap<String, Object>();
	private StateRequest stateRequest;
	
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
	 * Sill ok to use this object?
	 */
	void checkState() {
		if (isExpired) {
			if (conn.getStatus() != Connection.Status.OPEN)
				throw new ConnectionClosedException();
			else
				throw new SessionExpiredException();
		}
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
	 */
	@Override
	public StateRequest targetForState(State state) {

		checkState();

		// Can't have two requests at one time
		if (coreSession.getStateRequest() != null && !coreSession.getStateRequest().isCommitted()) {
			throw new ClientException.User(ACTIVE_REQUEST);
		}
		
		Payload.Session payload = conn.getServer().requestCreate(getId(), state.getName());

		// Server returns the new CoreSession object which reflects the targeted state.
		replaceCoreSession(payload.session);
		return new StateRequestImpl(coreSession.getStateRequest(), this);
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

	@Override
	public Object clearAttribute(String name) {
		return attributeMap.remove(name);
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
	public Date getCreateDate() {
		checkState();
		return coreSession.createDate();
	}

	@Override
	public Connection getConnection() {
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
		conn.getServer().eventSave(this, event);
	}
	
	@Override
	public long getTimeoutMillis() {
		return conn.getSessionTimeoutMillis();
	}


	@Override
	public StateRequest getStateRequest() {
		checkState();
		return stateRequest;
	}
	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	public void save() {
		conn.getServer().sessionSave(this);
	}
	
	/**
	 */
	public SessionIdTracker getSessionIdTracker() {
		return sessionIdTracker;
	}

	/**
	 */
	public TargetingTracker getTargetingTracker() {
		return targetingTracker;
	}

	/**
	 */
	public CoreSession getCoreSession() {
		return coreSession;
	}

	/**
	 */
	public void replaceCoreSession(CoreSession coreSession) {
		this.coreSession = (CoreSession) coreSession;
		targetingTracker.set(fromTargetingStabile(coreSession.getTargetingStabile()));
		CoreStateRequest coreRequest = coreSession.getStateRequest();
		this.stateRequest = coreRequest == null ? null : new StateRequestImpl(coreRequest, this);
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
