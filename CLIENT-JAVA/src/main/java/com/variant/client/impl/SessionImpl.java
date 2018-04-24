package com.variant.client.impl;

import static com.variant.client.ConfigKeys.TARGETING_TRACKER_CLASS_NAME;
import static com.variant.client.impl.ClientUserError.ACTIVE_REQUEST;
import static com.variant.client.impl.ClientUserError.TARGETING_TRACKER_NO_INTERFACE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.typesafe.config.Config;
import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.StateRequest;
import com.variant.client.TargetingTracker;
import com.variant.client.net.Payload;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.ConnectionStatus;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionScopedTargetingStabile;

/**
 * Permanent wrapper around transient CoreSession, + client only session functionality. 
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
	private StateRequestImpl stateRequest;

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
				result.add(e.getAsExperience(), e.getTimestamp());
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
			result.add(new TargetingTrackerEntryImpl(stabileEntry, this));
		return result;
	}
	
	/**
	 * Instantiate targeting tracker.
	 * 
	 * @param userData
	 * @return
	 */
	private TargetingTracker initTargetingTracker(Object...userData) {
		
		// Instantiate targeting tracker.
		String className = getConfig().getString(TARGETING_TRACKER_CLASS_NAME);
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof TargetingTracker) {
				TargetingTracker result = (TargetingTracker) object;
				result.init(this, userData);
				return result;
			}
			else {
				throw new ClientException.User(TARGETING_TRACKER_NO_INTERFACE, className, TargetingTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new ClientException.Internal("Unable to instantiate targeting tracker class [" + className +"]", e);
		}
	}
	
	/**
	 * Sill ok to use this object?
	 */
	void checkState() {
		if (isExpired) {
			if (conn.getStatus() != ConnectionStatus.OPEN)
				throw new ConnectionClosedException();
			else
				throw new SessionExpiredException();
		}
	}

	// ---------------------------------------------------------------------------------------------//
	//                                            PUBLIC                                            //
	// ---------------------------------------------------------------------------------------------//
	/**
	 * Original session creator.
	 */
	public SessionImpl(
			Connection conn,
			CoreSession coreSession,
			SessionIdTracker sessionIdTracker,
			Object...userData) {
		
		this.conn = (ConnectionImpl) conn;
		this.coreSession = coreSession;
		this.sessionIdTracker = sessionIdTracker;
		this.targetingTracker = initTargetingTracker(userData);
		this.coreSession.setTargetingStabile(toTargetingStabile(targetingTracker));
	}

	/**
	 * Parallel session creator.
	 */
	public SessionImpl(
			Connection conn,
			CoreSession coreSession) {
		
		this.conn = (ConnectionImpl) conn;
		this.coreSession = coreSession;
		this.sessionIdTracker = null;
		this.targetingTracker = null;
	}

	/**
	 *
	 */
	@Override
	public StateRequest targetForState(State state) {

		checkState();

		if (state == null) 
			throw new ClientException.User("State cannot be null");
		
		// Can't have two requests at one time
		if (coreSession.getStateRequest() != null && !coreSession.getStateRequest().isCommitted()) {
			throw new ClientException.User(ACTIVE_REQUEST);
		}
		
		Payload.Session payload = conn.client.server.requestCreate(getId(), state.getName(), conn);
		rewrap(payload.session);
		stateRequest = new StateRequestImpl(this);
		
		return stateRequest;
	}

	/**
	 * Mutable. But don't go to the server if already expired.
	 */
	@Override
	public boolean isExpired() {
		if (isExpired) return true;
		conn.refreshSession(this);
		return isExpired;
	}
	
	/**
	 * Immutable
	 */
	@Override
	public String getId() {
		return coreSession.getId();
	}

	/**
	 * Immutable
	 */
	@Override
	public Date getCreateDate() {
		checkState();
		return coreSession.createDate();
	}

	/**
	 * Immutable.
	 */
	@Override
	public Connection getConnection() {
		checkState();
		return conn;
	}

	/**
	 * Immutable
	 */
	@Override
	public Config getConfig() {
		checkState();
		return conn.getClient().getConfig();
	}

	/**
	 * Mutable
	 */
	@Override
	public Map<State, Integer> getTraversedStates() {
		checkState();
		return coreSession.getTraversedStates();
	}

	/**
	 * Mutable
	 */
	@Override
	public Set<Test> getTraversedTests() {
		checkState();
		return coreSession.getTraversedTests();
	}

	/**
	 * Mutable
	 */
	@Override
	public Set<Test> getDisqualifiedTests() {
		checkState();
		return coreSession.getDisqualifiedTests();
	}

	/**
	 * Mutable
	 */
	@Override
	public void triggerEvent(VariantEvent event) {
		checkState();
		conn.client.server.eventSave(this, event);
	}
	
	/**
	 * Immutable
	 */
	@Override
	public long getTimeoutMillis() {
		return conn.getSessionTimeoutMillis();
	}

	/**
	 * Mutable
	 */
	@Override
	public StateRequest getStateRequest() {
		checkState();
		return stateRequest;
	}

	/**
	 * Mutable
	 */
	@Override
	public String setAttribute(String name, String value) {
		checkState();
		String result = coreSession.setAttribute(name, value);
		save();
		return result;
	}    

	/**
	 * Read mutable
	 */
	@Override
	public String getAttribute(String name) {
		checkState();
		conn.refreshSession(this);
		return coreSession.getAttribute(name);
	}

	/**
	 * Mutate
	 */
	@Override
	public String clearAttribute(String name) {
		checkState();
		save();
		return coreSession.clearAttribute(name);
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//
	public void save() {
		conn.client.server.sessionSave(this);
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
		checkState();
		return coreSession;
	}

	/**
	 */
	public void rewrap(CoreSession coreSession) {
		// The new core session which this object wraps.
		this.coreSession = (CoreSession) coreSession;
		
		// Propagate to the state request wrapper object, if any.
		if(stateRequest != null) stateRequest.rewrap(coreSession.getStateRequest());
		
		targetingTracker.set(fromTargetingStabile(coreSession.getTargetingStabile()));
	}
	
	/**
	 * Expire this session object.
	 */
	public void expire() {
		isExpired = true;
	}

}
