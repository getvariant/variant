package com.variant.client.impl;

import static com.variant.client.VariantError.CANNOT_TRIGGER_SVE;
import static com.variant.client.VariantError.PARAM_CANNOT_BE_NULL;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionAttributes;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.StateRequest;
import com.variant.client.TargetingTracker;
import com.variant.client.TraceEvent;
import com.variant.client.VariantException;
import com.variant.client.util.MethodTimingWrapper;
import com.variant.share.schema.Schema;
import com.variant.share.schema.State;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;
import com.variant.share.session.CoreSession;
import com.variant.share.session.SessionScopedTargetingStabile;

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
		
		return new MethodTimingWrapper<StateRequest>().exec( () -> {
			server.requestCreate(this, state.getName());
			return getStateRequest().get();
		});
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
	public Instant getTimestamp() {
		return coreSession.getTimestamp();
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
		return new MethodTimingWrapper<Map<State, Integer>>().exec( () -> {
			refreshFromServer();
			return coreSession.getTraversedStates();
		});
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public Set<Variation> getTraversedVariations() {
		preChecks();
		return new MethodTimingWrapper<Set<Variation>>().exec( () -> {
			refreshFromServer();
			return coreSession.getTraversedVariations();
		});
	}

	@Override
	public Set<Experience> getLiveExperiences() {
		preChecks();
		return new MethodTimingWrapper<Set<Experience>>().exec( () -> {
			refreshFromServer();
			return coreSession.getTargetingStabile().getAllAsExperiences(schema);
		});
	}

	@Override
	public Optional<Experience> getLiveExperience(Variation variation) {
		return getLiveExperience(variation.getName());
	}

	@Override
	public Optional<Experience> getLiveExperience(String variationName) {
		preChecks();
		return new MethodTimingWrapper<Optional<Experience>>().exec( () -> {
			refreshFromServer();
			return Optional.ofNullable(coreSession.getTargetingStabile().getAsExperience(variationName, schema));
		});
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public Set<Variation> getDisqualifiedVariations() {
		preChecks();
		return new MethodTimingWrapper<Set<Variation>>().exec( () -> {
			refreshFromServer();
			return coreSession.getDisqualifiedVariations();
		});
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public void triggerTraceEvent(TraceEvent event) {
		preChecks();
		
		if (event instanceof StateVisitedEvent) 
			throw new VariantException(CANNOT_TRIGGER_SVE);
		new MethodTimingWrapper<Object>().exec( () -> {
			conn.client.server.eventSave(this, event);
			return null;  // A vid method, really. Need this to make the compiler happy.
		});
	}
	
	/**
	 * State request is a local object.
	 */
	@Override
	public Optional<StateRequest> getStateRequest() {
		preChecks();
		return Optional.ofNullable(stateRequest);
	}

	/**
	 * Mutating or mutable state.
	 */
	@Override
	public SessionAttributes getAttributes() {
		preChecks();
		return new MethodTimingWrapper<SessionAttributes>().exec( () -> {
			refreshFromServer();
			return new SessionAttributesImpl(this);
		});
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//	
	/**
	 * Sill ok to use this object?
	 * Package visibility because state request abject needs access.
	 */
	public void preChecks() {
		if (isExpired) throw new SessionExpiredException(coreSession.getId());
	}

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
			throw VariantException.internal("Null Core Session");
		
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
