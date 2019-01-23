package com.variant.client.impl;

import static com.variant.client.impl.ClientUserError.CANNOT_TRIGGER_SVE;
import static com.variant.client.impl.ClientUserError.PARAM_CANNOT_BE_NULL;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.StateRequest;
import com.variant.client.TargetingTracker;
import com.variant.client.VariantException;
import com.variant.client.session.TargetingTrackerEntryImpl;
import com.variant.core.TraceEvent;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionScopedTargetingStabile;

/**
 * Permanent wrapper around transient CoreSession, + client only session functionality. 
 * 
 * @author Igor
 *
 */
public class SessionImpl implements Session {

	final private static Logger LOG = LoggerFactory.getLogger(SessionImpl.class);
		
	private final ConnectionImpl conn;  // Connection which created this session.
	private final Server server;        // Server object associated with this VariantClient.
	private final Schema schema;
	private CoreSession coreSession;
	private StateRequestImpl stateRequest;

	// package scope -- set in ConnectionImpl
	SessionIdTracker sessionIdTracker = null;
	public TargetingTracker targetingTracker = null;

	public boolean isExpired = false;

	/**
	 * 
	 * @param tt
	 * @return
	 *
	private SessionScopedTargetingStabile toTargetingStabile(TargetingTracker tt) {

		SessionScopedTargetingStabile result = new SessionScopedTargetingStabile();
		Collection<TargetingTracker.Entry> entries = tt.get();
		if (entries != null)
			for (TargetingTracker.Entry e : entries)
				result.add(e.getExperience(), e.getTimestamp());
		return result;
	}
*/
	/**
	 * 
	 * @param stabile
	 * @return
	 */
	private Set<TargetingTracker.Entry> fromTargetingStabile(SessionScopedTargetingStabile stabile) {
		HashSet<TargetingTracker.Entry> result = new HashSet<TargetingTracker.Entry>(stabile.size());
		for (SessionScopedTargetingStabile.Entry stabileEntry: stabile.getAll()) 
			result.add(new TargetingTrackerEntryImpl(stabileEntry));
		return result;
	}
	
	/**
	 * Sill ok to use this object?
	 * Package visibility because state request abject needs access.
	 */
	void preChecks() {
		if (isExpired) throw new SessionExpiredException(coreSession.getId());
	}

	// ---------------------------------------------------------------------------------------------//
	//                                            PUBLIC                                            //
	// ---------------------------------------------------------------------------------------------//
	/**
	 * Create a headless session without the session ID and the targeting trackers.
	 * May contain state request object.
	 */
	public SessionImpl(
			Connection conn,
			Schema schema,
			CoreSession coreSession) {
		
		this.conn = (ConnectionImpl) conn;
		this.schema = schema;
		this.server = this.conn.client.server;
		this.coreSession = coreSession;
		this.sessionIdTracker = null;
		this.targetingTracker = null;
		
		if (coreSession.getStateRequest().isPresent()) {
			// relies on `this` already containing the coreSession object.
			this.stateRequest = new StateRequestImpl(this);
		}
	}

	/**
	 * Target this session for a given state.
     * State visited event is not part of the session's shared state, so we create it here
     * and it remains a local object, until it gets sent to the server with request commit.
	 */
	@Override
	public StateRequest targetForState(State state) {

		preChecks();

		if (state == null) 
			throw new VariantException(PARAM_CANNOT_BE_NULL, "state");
		
		server.requestCreate(this, state.getName());
		return getStateRequest().get();
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
	public Set<Variation> getTraversedVariations() {
		preChecks();
		refreshFromServer();
		return coreSession.getTraversedVariations();
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public Set<Variation> getDisqualifiedVariations() {
		preChecks();
		refreshFromServer();
		return coreSession.getDisqualifiedVariations();
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public void triggerTraceEvent(TraceEvent event) {
		preChecks();
		
		if (event instanceof StateVisitedEvent) 
			throw new VariantException(CANNOT_TRIGGER_SVE);
		
		conn.client.server.eventSave(this, event);
	}
	
	/**
	 * State request is a local object.
	 */
	@Override
	public Optional<StateRequest> getStateRequest() {
		return Optional.ofNullable(stateRequest);
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public Map<String,String> getAttributes() {
		preChecks();
		refreshFromServer();
		return new SessionAttributeMap(this);
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//	
	
	/**
	 */
	public void  expire() {
		isExpired = true;
	}

	/**
	 */
	public CoreSession getCoreSession() {
		preChecks();
		return coreSession;
	}

	/**
	 */
	public Server getServer() {
		preChecks();
		return server;
	}

	/**
	 * Save trackers. Headless sessions won't have them.
	 */
	public void saveTrackers(Object... userData) {
		if (targetingTracker != null) targetingTracker.save(userData);
		if (sessionIdTracker != null) sessionIdTracker.save(userData);
	}

	/**
	 * 
	 */
	public void clearStateRequest() {
		stateRequest = null;
	}
	
	/**
	 * Replace the core session. Recursively replaces the core state request.
	 */
	public void rewrap(CoreSession coreSession) {
		
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Rewrapping session ID [%s] in [%s]", getId(), this));
		}
		
		if (coreSession == null) 
			throw new VariantException.Internal("Null Core Session");
		
		// The new core session which this object wraps.
		this.coreSession = (CoreSession) coreSession;
		
		// Propagate to the state request wrapper object, if any.
		// Relies on this SessionImpl already containing the new core session.
		if(stateRequest == null) {
			if (coreSession.getStateRequest().isPresent())
				stateRequest = new StateRequestImpl(this);
		}
		else {
			stateRequest.rewrap(coreSession.getStateRequest().get());			
		}
		
		// Update targeting tracker, if a foreground session.
		if (targetingTracker != null)
			targetingTracker.set(fromTargetingStabile(coreSession.getTargetingStabile()));
		
	}
	
	/**
	 * Refresh this session from server.
	 */
	public void refreshFromServer() {
		conn.refreshSession(this);
	}
	
	@Override
	public Schema getSchema() {
		return schema;
	}

}
