package com.variant.client;

import java.util.Map;
import java.util.Set;

import com.variant.core.TraceEvent;
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
	 * The Variant session associated with this request. This is the session whose {@link Session#targetForState(State)}
	 * object produced this object..
	 * 
	 * @return An object of type {@link Session}.
	 * @since 0.6
	 */
	public Session getSession();
	
	/**
	 * The target {@link State} of this request, which was passed to {@link Session#targetForState(State)}.
	 * 
	 * @return An object of type {@link State}
	 * @since 0.6
	 */
	State getState();
	
	/**
	 * The state variant to which this state request resolved at run time. A state request can 
	 * have either trivial resolution, or resolve to a {@link StateVariant}. Trivial resolution means that all live
	 * experiences are control experiences, and the user session will be targeted for the base state. If at least one
	 * live experience is a variant, the targeting operation will resolve to some state variant definition in the schema.
	 * 
	 * @return The {@link StateVariant} to which this request resolved, or null if all live experiences are control.
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
	 * @return Immutable map keyed by parameter names. Empty map, if no state parameters were declared.
	 * @since 0.7
	 */	
	Map<String, String> getResolvedParameters();

	/**
	 * <p>Targeted test experiences in tests instrumented on this state.
	 * An experience is live iff this session has been targeted for it and its containing test
	 * is instrumented on this state and is neither off nor disqualified in this session.
	 * Both, variant and non-variant instrumentations are included.
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
	 *  This is useful if the caller wants to add parameters to this event before it is triggered.
	 *  
	 * @return Object of type {@link TraceEvent}.
	 * @since 0.6
	 */
	TraceEvent getStateVisitedEvent();
		
	/**
	 * Commit this state request.
     * The associated state visited {@link TraceEvent} is triggered.
     * No-op if this request has already been committed in this or a parallel session.
	 * 
	 * @param userData   An array of zero or more opaque objects which will be passed to {@link SessionIdTracker#save(Object...)}
	 * and {@link TargetingTracker#save(Object...)} methods without interpretation.
	 */
	void commit(Object...userData);
	
	/**
	 * Has this state request been committed?  A local operation, i.e. may not reflect the current state of this request
	 * if already committed in a parallel session.
     * 
     *@return true if this request has ben committed, or false otherwise.
	 * @since 0.6
	 */
	boolean isCommitted();
	
}
