package com.variant.core;

import java.util.Collection;
import java.util.Map;

import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Represents a state request, as instantiated by {@link com.variant.core.Variant#newStateRequest(VariantSession, State, Object)}.
 * 
 * @author Igor Urisman
 * @since 0.5
 *
 */
public interface VariantStateRequest {

	/**
	 * This request's Variant session.
	 * 
	 * @return Variant session as an instance of {@link com.variant.core.VariantSession}.
	 * @since 0.5
	 */
	public VariantSession getSession();
	
	/**
	 * The state for which this request was generated, i.e. that was passed to 
	 * {@link com.variant.core.Variant#newStateRequest(VariantSession, State, Object)}.
	 * 
	 * @return State as an instance of {@link com.variant.core.schema.State}
	 * @since 0.5
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
	 * @since 0.5
	 */
	public Map<String,String> getResolvedParameterMap();
		
	/**
	 * The implementation of {@link com.variant.core.VariantTargetingTracker} used by this session
	 * to maintain the list of targeted tests.
	 * 
	 * @return An instance of type  {@link com.variant.core.VariantTargetingTracker}.
	 * @since 0.5
	 */
	public VariantTargetingTracker getTargetingTracker();

	/**
	 * Get all targeted experiences. Only variant experiences from active tests are included. 
	 * Control experiences, OFF tests or disqualified tests are not included.
	 * 
	 * @return Collection of {@link com.variant.core.schema.Test.Experience}s.
	 * @since 0.5
	 */
	public Collection<Experience> getTargetedExperiences();

	/**
	 * The targeted experience in a given test.
	 * 
	 * @param test {@link com.variant.core.schema.Test}
	 * @return The {@link com.variant.core.schema.Test.Experience} of the given 
	 *         {@link com.variant.core.schema.Test} targeted by this session or null if none. 
	 *         The latter condition is possible when the state in this request is not instrumented 
	 *         by this test, or the targeted experience is control, or the test is off, or this
	 *         session is disqualified for this test. 
	 *  
	 * @since 0.5
	 */
	public Experience getTargetedExperience(Test test);

	/**
	 * All tests for which this session has been disqualified.
	 * @see com.variant.core.flashpoint.TestQualificationFlashpoint
	 * 
	 * @return Collection of objects of type {@link com.variant.core.schema.Test}
	 * @since 0.5
	 */
	public Collection<Test> getDisqualifiedTests();

	/**
	 * All pending events that will be flushed when this request is committed.
	 * 
	 * @return Collection of {@link {@link com.variant.core.event.VariantEvent}s.
	 * @see com.variant.core.Variant#commitStateRequest(VariantStateRequest, Object...).
	 * @since 0.5
	 */
	public Collection<VariantEvent> getPendingEvents();
	
	/**
	 * Set the status of this request.
	 * 
	 * @param status {@link Status}
	 */
	public void setStatus(Status status);
	
	/**
	 * Status of a {@link com.variant.core.VariantStateRequest}.
	 */
	public static enum Status {
		OK, FAIL
	}
}
