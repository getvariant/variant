package com.variant.core;

import java.util.Collection;
import java.util.Map;

import com.variant.core.event.VariantEvent;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Represents a state request, as instantiated by {@link com.variant.core.Variant#targetForState(VariantCoreSession, State, Object)}.
 * 
 * @author Igor Urisman
 * @since 0.6
 *
 */
public interface VariantCoreStateRequest {

	/**
	 * This request's Variant session.
	 * 
	 * @return Variant session as an instance of {@link com.variant.core.VariantCoreSession}.
	 * @since 0.6
	 */
	public VariantCoreSession getSession();
	
	/**
	 * The state for which this request was generated, i.e. that was passed to 
	 * {@link com.variant.core.Variant#targetForState(VariantCoreSession, State, Object)}.
	 * 
	 * @return State as an instance of {@link com.variant.core.schema.State}
	 * @since 0.6
	 */
	public State getState();
	
	/**
	 * The parameter map to which this state request resolved. As Variant resolves a state,
	 * it identifies a {@link com.variant.core.schema.Test.OnState.Variant} of the requested 
	 * base {@link com.variant.core.schema.State}, which the user session will be served instead of
	 * the requested state. The resolved parameter map contains the
	 * base {@link com.variant.core.schema.State}'s parameter map, merged with the resolved
	 * {@link com.variant.core.schema.Test.OnState.Variant}'s map: if a parameter is present in
	 * both maps, the merged value is that from the {@link com.variant.core.schema.Test.OnState.Variant}'s
	 * map.
	 * 
	 * @return Resolved parameter map.
	 * @since 0.6
	 */
	public Map<String,String> getResolvedParameterMap();
		
	/**
	 * The implementation of {@link com.variant.core.VariantTargetingTracker} used by this session
	 * to maintain the list of targeted tests.
	 * 
	 * @return An instance of type  {@link com.variant.core.VariantTargetingTracker}.
	 * @since 0.6
	 *
	public VariantTargetingTracker getTargetingTracker();
	should live in session because we say that session scoped targeting stability is automatically guaranteed.
	should probably live on client.
	*/

	/**
	 * Get all targeted experiences from active tests. A test is active if it has been
	 * traversed by the current session. Off tests and disqualified tests are excluded,
	 * as are control-only experiences on current state.
	 * 
	 * @return Collection of {@link com.variant.core.schema.Test.Experience}s.
	 * @since 0.6
	 */
	public Collection<Experience> getTargetedExperiences();

	/**
	 * The targeted experience in a given test, if any.
	 * 
	 * @param test {@link com.variant.core.schema.Test}
	 * @return The {@link com.variant.core.schema.Test.Experience} of the given 
	 *         {@link com.variant.core.schema.Test} targeted by this session or null if none. 
	 *         The latter condition is possible when the targeted experience is control, 
	 *         or the test is off, or this session is disqualified for this test. 
	 *         
	 * @throws VariantRuntimeException if given test is not instrumented by this requests's test.
	 * 
	 * @since 0.6
	 */
	public Experience getTargetedExperience(Test test);
		
	/** Get the pending state visited event. This is useful if the caller wants to add parameters to this
	 *  event before it is flushed to external storage.
	 * @return Pending event of type {@link com.variant.core.event.VariantEvent} or null if this request has already
	 *         been committed;
	 * @since 0.6
	 */
	public VariantEvent getStateVisitedEvent();
	
	/**
	 * Set the status of this request.
	 * 
	 * @param status {@link Status}
	 */
	public void setStatus(Status status);
	
	/**
	 * Commit this state request. Flushes to storage this session's state. 
	 * See the Variant RCE User Guide for more information about Variant session
     * life cycle.
     * 
	 * @param request The state request to be committed.
     *
	 * @since 0.6
	 */
	public void commit();

	/**
	 * Current status of this request.
	 */
	public Status getStatus();

	/**
	 * Status of a {@link com.variant.core.VariantCoreStateRequest}.
	 */
	public static enum Status {
		OK, FAIL
	}
}
