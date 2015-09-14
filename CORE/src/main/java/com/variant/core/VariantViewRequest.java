package com.variant.core;

import java.util.Collection;
import java.util.Map;

import com.variant.core.impl.StateServeEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.TargetingPersister;

/**
 * Encapsulates state relevant to a single view request.
 * 
 * @author Igor
 *
 */
public interface VariantViewRequest {

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
	 * View serve event associated with this view request
	 * @return
	 */
	public StateServeEvent getViewServeEvent();

	/**
	 * 
	 * @return
	 */
	public TargetingPersister getTargetingPersister();

	/**
	 * Get all experience targeted in this request.
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
