package com.variant.core;

import java.util.Collection;
import java.util.Map;

import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Variant Core state request.
 * 
 * @author Igor Urisman
 * @since 0.6
 *
 */
public interface VariantCoreStateRequest {

	/**
	 * The Variant session that obtained this request via {@link VariantCoreSession#targetForState(State)}.
	 * 
	 * @return An object of type {@link VariantCoreSession}.
	 * @since 0.6
	 */
	public VariantCoreSession getSession();
	
	/**
	 * The {@link State} of this request, which was passed to {@link VariantCoreSession#targetForState(State)}.
	 * 
	 * @return An object of type {@link State}
	 * @since 0.6
	 */
	public State getState();
	
	/**
	 * The parameter map of the state variant to which this state request resolved.
	 * 
	 * @return Resolved parameter map.
	 * @since 0.6
	 * @see StateVariant
	 */
	public Map<String,String> getResolvedParameterMap();
		
	/**
	 * <p>This session's all live experiences on this state.
	 * An experience is live iff this session has been targeted for it and its containing test
	 * is instrumented on this state and is neither off nor disqualified in this session.
	 * Both, variantful and non-variant instrumentations are included.
	 * 
	 * @return Collection of {@link Test.Experience}s.
	 * @since 0.6
	 */
	public Collection<Experience> getLiveExperiences();

	/**
	 * The live experience in a given test, if any. See {@link #getLiveExperiences()} for
	 * definition of live experience.
	 * 
	 * @param test {@link Test}
	 * @return An object of type {@link Experience}. 
	 * 
	 * @since 0.6
	 */
	public Experience getLiveExperience(Test test);
		
	/** Pending state visited event. 
	 *  This is useful if the caller wants to add parameters to this event before it is flushed to external storage.
	 *  
	 * @return Object of type {@link VariantEvent}.
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
	 * Commit this state request.
     * The pending state visited {@link VariantEvent} is triggered.
     *
	 * @since 0.6
	 */
	public void commit();

	/**
	 * Is this request object represent a request that has been committed?
     * 
     *@return true if this request has ben committed, or false otherwise.
	 * @since 0.6
	 */
	public boolean isCommitted();

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
