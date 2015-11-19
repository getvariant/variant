package com.variant.core;

import java.util.Collection;
import java.util.Map;

import com.variant.core.impl.StateServeEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.TargetingTracker;

/**
 * Encapsulates state relevant to a single state request.
 * 
 * @author Igor
 *
 */
public interface VariantStateRequest {

	/**
	 * This request's Variant session.
	 * @return
	 */
	public VariantSession getSession();
	
	/**
	 * The state for which this request was generated.
	 * @return
	 */
	public State getState();
	
	/**
	 * The parameter map to which this request resolved.
	 * 
	 * @return A variant's merged parameter map if resolved to a variant
	 *         or state's parameter map, if resolved to control.
	 */
	public Map<String,String> getResolvedParameterMap();
		
	/**
	 * State serve event associated with this view request
	 * @return
	 */
	public StateServeEvent getStateServeEvent();

	/**
	 * 
	 * @return
	 */
	public TargetingTracker getTargetingPersister();

	/**
	 * Get all experience targeted in this request.
	 * Only actually targeted experiences are included. Control experiences
	 * for OFF or disqualified tests are not included.
	 * @return
	 */
	public Collection<Experience> getTargetedExperiences();

	/**
	 * Get the experience targeted in this request for a particular test.
	 * @param test
	 * @return
	 */
	public Experience getTargetedExperience(Test test);

	/**
	 * All tests that have been disqualified for this request by TestQualificationFlashpoint listeners.
	 * 
	 * @return
	 */
	public Collection<Test> getDisqualifiedTests();
	
	/**
	 * 
	 * @param status
	 */
	public void setStatus(Status status);
	
	/**
	 * 
	 */
	public static enum Status {
		OK, FAIL
	}
}
