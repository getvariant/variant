package com.variant.client.impl;

import static com.variant.client.impl.ClientUserError.ACTIVE_REQUEST;
import static com.variant.client.impl.ClientUserError.TARGETING_TRACKER_NO_INTERFACE;
import static com.variant.client.impl.ConfigKeys.TARGETING_TRACKER_CLASS_NAME;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.StateRequest;
import com.variant.client.TargetingTracker;
import com.variant.client.lifecycle.LifecycleEvent;
import com.variant.client.lifecycle.LifecycleHook;
import com.variant.client.lifecycle.SessionExpired;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.ConnectionStatus;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionScopedTargetingStabile;
import com.variant.core.util.immutable.ImmutableList;

/**
 * Permanent wrapper around transient CoreSession, + client only session functionality. 
 * 
 * @author Igor
 *
 */
public class SessionImpl implements Session {

	final private static Logger LOG = LoggerFactory.getLogger(SessionImpl.class);
	
	private boolean isExpired = false;
	
	private final ConnectionImpl conn;  // Connection which created this session.
	private final Server server;        // Server object associated with this VariantClient.
	private CoreSession coreSession;
	private SessionIdTracker sessionIdTracker;
	private TargetingTracker targetingTracker;
	private StateRequestImpl stateRequest;

	// Lifecycle hooks.
	final private ArrayList<LifecycleHook<? extends LifecycleEvent>> lifecycleHooks = 
			new ArrayList<LifecycleHook<? extends LifecycleEvent>>();

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
	private Set<TargetingTracker.Entry> fromTargetingStabile(SessionScopedTargetingStabile stabile) {
		HashSet<TargetingTracker.Entry> result = new HashSet<TargetingTracker.Entry>(stabile.size());
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
	 * Package visibility because state request abject needs access.
	 */
	void preChecks() {
		if (isExpired) {
			if (conn.getStatus() == ConnectionStatus.DRAINING)
				throw new ConnectionClosedException(ClientUserError.CONNECTION_DRAINING);
			else if (conn.getStatus() != ConnectionStatus.OPEN)
				throw new ConnectionClosedException(ClientUserError.CONNECTION_CLOSED);
			else
				throw new SessionExpiredException();
		}
	}

	// ---------------------------------------------------------------------------------------------//
	//                                            PUBLIC                                            //
	// ---------------------------------------------------------------------------------------------//
	/**
	 * Create a brand new foreground session with a session ID and a targeting trackers.
	 */
	public SessionImpl(
			Connection conn,
			CoreSession coreSession,
			SessionIdTracker sessionIdTracker,
			Object...userData) {
		
		this.conn = (ConnectionImpl) conn;
		this.server = this.conn.client.server;
		this.coreSession = coreSession;
		this.sessionIdTracker = sessionIdTracker;
		this.targetingTracker = initTargetingTracker(userData);
		this.coreSession.setTargetingStabile(toTargetingStabile(targetingTracker));
	}

	/**
	 * Create a headless session without the session ID and the targeting trackers.
	 * May contain state request object.
	 */
	public SessionImpl(
			Connection conn,
			CoreSession coreSession) {
		
		this.conn = (ConnectionImpl) conn;
		this.server = this.conn.client.server;
		this.coreSession = coreSession;
		this.sessionIdTracker = null;
		this.targetingTracker = null;
		
		if (coreSession.getStateRequest() != null) {
			// relies on `this` already containing the coreSession object.
			this.stateRequest = new StateRequestImpl(this);
		}
	}

	/**
	 *
	 */
	@Override
	public StateRequest targetForState(State state) {

		preChecks();

		if (state == null) 
			throw new ClientException.User("State cannot be null");
		
		// Can't have two requests at one time
		if (coreSession.getStateRequest() != null && !coreSession.getStateRequest().isCommitted()) {
			throw new ClientException.User(ACTIVE_REQUEST);
		}
		server.requestCreate(this, state.getName());
		return getStateRequest();
	}

	/**
	 * Mutable. But don't go to the server if already expired.
	 * Do not throw connection expired exception.
	 */
	@Override
	public boolean isExpired() {
		
		if (isExpired) return true;
		
		try {
			refreshFromServer();
		}
		catch (SessionExpiredException | ConnectionClosedException e) { 
			isExpired = true; 
		}
		
		return isExpired;
	}
	
	/**
	 * Non-mutating
	 */
	@Override
	public String getId() {
		return coreSession.getId();
	}

	/**
	 * Non-mutating
	 */
	@Override
	public Date getCreateDate() {
		return coreSession.createDate();
	}

	/**
	 * Non-mutating
	 */
	@Override
	public Connection getConnection() {
		return conn;
	}

	/**
	 * Non-mutating, 
	 * but connection may refuse.
	 */
	@Override
	public Config getConfig() {
		return conn.getClient().getConfig();
	}

	/**
	 * Immutable
	 */
	@Override
	public long getTimeoutMillis() {
		return conn.getSessionTimeoutMillis();
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public Map<State, Integer> getTraversedStates() {
		preChecks();
		refreshFromServer();
		return coreSession.getTraversedStates();
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public Set<Test> getTraversedTests() {
		preChecks();
		refreshFromServer();
		return coreSession.getTraversedTests();
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public Set<Test> getDisqualifiedTests() {
		preChecks();
		refreshFromServer();
		return coreSession.getDisqualifiedTests();
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public void triggerEvent(VariantEvent event) {
		preChecks();
		conn.client.server.eventSave(this, event);
	}
	
	/**
	 * Mutating or mutable state.
	 */
	@Override
	public StateRequest getStateRequest() {
		preChecks();
		refreshFromServer();
		return stateRequest;
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public String setAttribute(String name, String value) {
		if (name == null) throw new ClientException.User("Name cannot be null");
		if (value == null) throw new ClientException.User("Value cannot be null");
		preChecks();
		return server.sessionAttrSet(this, name, value);
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public String getAttribute(String name) {
		if (name == null) throw new ClientException.User("Name cannot be null");
		preChecks();
		refreshFromServer();
		return coreSession.getAttribute(name);
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public String clearAttribute(String name) {
		if (name == null) throw new ClientException.User("Name cannot be null");
		preChecks();
		return server.sessionAttrClear(this, name);
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public void addLifecycleHook(LifecycleHook<? extends LifecycleEvent> hook) {
		preChecks();
		refreshFromServer();
		lifecycleHooks.add(hook);
	}


	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//
	public void save() {
		conn.client.server.sessionSave(this);
	}

	/**
	 */
	public CoreSession getCoreSession() {
		preChecks();
		return coreSession;
	}

	/**
	 * Save trackers. Headless sessions won't have them.
	 */
	public void saveTrackers(Object... userData) {
		if (targetingTracker != null) targetingTracker.save(userData);
		if (sessionIdTracker != null) sessionIdTracker.save(userData);
	}

	/**
	 * Replace the core session. Recursively replaces the core state request.
	 */
	public void rewrap(CoreSession coreSession) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Rewrapping session ID [%s] in [%s]", getId(), this));
		}
		
		if (coreSession == null) 
			throw new ClientException.Internal("Null Core Session");
		
		// The new core session which this object wraps.
		this.coreSession = (CoreSession) coreSession;
		
		// Propagate to the state request wrapper object, if any.
		// Relies on this SessionImpl already containing the new core session.
		if(stateRequest == null) {
			if (coreSession.getStateRequest() != null)
				stateRequest = new StateRequestImpl(this);
		}
		else {
			stateRequest.rewrap(coreSession.getStateRequest());			
		}
		
		// Update targeting tracker, if a foreground session.
		if (targetingTracker != null)
			targetingTracker.set(fromTargetingStabile(coreSession.getTargetingStabile()));
		
	}
	
	/**
	 * Expire this session object.
	 */
	public void expire() {
		
		if (LOG.isDebugEnabled()) LOG.debug("Expired session [" + getId() + "]");

		isExpired = true;
		((VariantClientImpl)conn.getClient()).lceService.raiseEvent(SessionExpired.class, this);
	}

	/**
	 * Refresh this session from server.
	 */
	public void refreshFromServer() {
		conn.refreshSession(this);
	}
	
	/**
	 * Read-only snapshot.
	 */
	public ImmutableList<LifecycleHook<? extends LifecycleEvent>> getLifecycleHooks() {
		return new ImmutableList<LifecycleHook<? extends LifecycleEvent>>(lifecycleHooks);
	}

}
