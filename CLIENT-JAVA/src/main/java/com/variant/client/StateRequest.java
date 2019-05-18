package com.variant.client;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.core.session.StateRequestStatus;

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
	 * The current status of this state request.
     * 
     * @return An object of type {@link StateRequestStatus}.
	 * 
	 * @since 0.9
	 */
	StateRequestStatus getStatus();

	/**
	 * The state variant to which this state request resolved at run time. A state request can 
	 * have either trivial resolution, or resolve to a {@link StateVariant}. Trivial resolution means that all live
	 * experiences are control experiences, and the user session will be targeted for the base state. If at least one
	 * live experience is a variant, the targeting operation will resolve to some state variant definition in the schema.
	 * 
	 * @return The {@link Optional} containing the {@link StateVariant} object to which this request resolved, 
	 *         or empty if all live experiences on this state are control.
	 * 
	 * @since 0.6
	 * @see StateVariant
	 */
	Optional<StateVariant> getResolvedStateVariant();
		
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
	 * <p>Targeted experiences in variations instrumented on this state.
	 * An experience is live iff this session has been targeted for it and its containing variation
	 * is instrumented on this state, and is neither off nor disqualified for in this session.
	 * 
	 * @return Collection of {@link Test.Experience}s. The collection will be of size 0 if no live experiences
	 *         are instrumented on this state.
	 * @since 0.6
	 */
	Set<Experience> getLiveExperiences();

	/**
	 * The live experience in a given test, if any. See {@link #getLiveExperiences()} for
	 * definition of live experience. 
	 * 
	 * @param variation {@link Variation} of interest.
	 * @return An {@link Optional} of {@link Experience}, containing the live experience
	 *         in the given variation, or empty if this this variation is off or does not 
	 *         instrument this state.
	 * 
	 * @since 0.6
	 */
	Optional<Experience> getLiveExperience(Variation variation);

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
	 * 
	 * @since 0.6
	 */
	void commit(Object...userData);
	
	/**
	 * Fail this state request.
     * The associated state visited {@link TraceEvent} is triggered.
     * No-op if this request has already been failed in this or a parallel session.
	 * 
	 * @param userData   An array of zero or more opaque objects which will be passed to {@link SessionIdTracker#save(Object...)}
	 * and {@link TargetingTracker#save(Object...)} methods without interpretation.
	 * 
	 * @since 0.9
	 */
	void fail(Object...userData);
	
	/**
	 * The current status of a state request. 
	 * @since 0.10
	 */
	public enum Status {

		InProgress, Committed, Failed;

		/**
		 * Is a value one of the given values?
		 * 
		 * @param statuses
		 * @return ture if this value is one of the given values, false otherwise.
		 *
		public boolean isIn(StateRequestStatus... statuses) {
			
			for (StateRequestStatus s: statuses) 
				if (this == s) return true;
			return false;
		}
		*/
	}	
}
