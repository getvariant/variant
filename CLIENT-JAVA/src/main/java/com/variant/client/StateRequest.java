package com.variant.client;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.variant.core.StateRequestStatus;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Represents a Variant state request, as returned by {@link Session#targetForState(State)}.
 *
 * 
 * @author Igor Urisman
 * @since 0.6
 *
 */
public interface StateRequest {

	/**
	 * The Variant session that obtained this request via {@link Session#targetForState(State)}.
	 * 
	 * @return An object of type {@link Session}.
	 * @since 0.6
	 */
	public Session getSession();
	
	/**
	 * The target {@link State} of this request, which was passed to {@link VariantCoreSession#targetForState(State)}.
	 * 
	 * @return An object of type {@link State}
	 * @since 0.6
	 */
	State getState();
	
	/**
	 * The state variant to which this state request resolved at run time. At run time, a state request can
	 * either have trivial resolution, or resolve to a {@link StateVariant}. Trivial resolution means that all live
	 * experiences are control experiences, and the user session will be targeted for the base state. If at least one
	 * live experience is a variant, the targeting operation will resolve to some state variant definition in the schema.
	 * 
	 * @return The state variant to which this request resolved, or null if all live experiences are control.
	 * 
	 * @since 0.6
	 * @see StateVariant
	 */
	StateVariant getResolvedStateVariant();
		
	/**
	 * The resolved state parameters as an immutable map. In case of trivial resolution, 
	 * resolved state parameters are the ones declared at the state level. In the case of 
	 * non-trivial resolution, the parameters declared at the {@link StateVariant} level override
	 * likely-named state parameters declared at the state level.  
	 * 
	 * @param name Parameter name.
	 * @return Immutable map keyed by parameter names. Empty, if no state parameters were declared.
	 * @since 0.7
	 */	
	Map<String, String> getResolvedParameters();

	/**
	 * <p>This session's all live experiences on this state.
	 * An experience is live iff this session has been targeted for it and its containing test
	 * is instrumented on this state and is neither off nor disqualified in this session.
	 * Both, variantful and non-variant instrumentations are included.
	 * 
	 * @return Collection of {@link Test.Experience}s.
	 * @since 0.6
	 */
	Set<Experience> getLiveExperiences();

	/**
	 * The live experience in a given test, if any. See {@link #getLiveExperiences()} for
	 * definition of live experience. Throws the {@link StateNotInstrumentedException} if the
	 * target state of this request, i.e. given by {@link #getState()}, is not instrumented 
	 * by the given test. 
	 * 
	 * @param test {@link Test}
	 * @return An object of type {@link Experience}.
	 * 
	 * @since 0.6
	 */
	Experience getLiveExperience(Test test);

	/** Pending state visited event. 
	 *  This is useful if the caller wants to add parameters to this event before it is flushed to external storage.
	 *  
	 * @return Object of type {@link VariantEvent}.
	 * @since 0.6
	 */
	VariantEvent getStateVisitedEvent();
	
	/**
	 * Set the status of this request.
	 * 
	 * @param status
	 * @since 0.6
	 */
	void setStatus(StateRequestStatus status);
	
	/**
	 * Current status of this request.
	 * 
	 * @return Status of this request.
	 */
	StateRequestStatus getStatus();
	
	/**
	 * Commit this state request.
     * The pending state visited {@link VariantEvent} is triggered. 
     * No-op if this request has already been committed, i.e. okay to call multiple times.
	 * 
	 * @param userData   An array of zero or more opaque objects which will be passed to {@link SessionIdTracker#save(Object...)}
	 * and {@link TargetingTracker#save(Object...)} methods without interpretation.
	 */
	boolean commit(Object...userData);
	
	/**
	 * Is this request object represent a request that has been committed?
     * 
     *@return true if this request has ben committed, or false otherwise.
	 * @since 0.6
	 */
	boolean isCommitted();

	/**
	 * Creation timestamp.
     * 
     *@return Creation timestamp of this state request object.
	 * @since 0.7
	 */
	public Date createDate();
	
}
