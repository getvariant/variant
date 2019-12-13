package com.variant.client;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.share.schema.State;
import com.variant.share.schema.StateVariant;
import com.variant.share.schema.Variation;
import com.variant.share.schema.Variation.Experience;

/**
 * Variant state request, as returned by {@link Session#targetForState(State)}. Each state request must be completed with
 * either {@link #commit(Object...)} or {@link #fail(Object...)} before this session can to target another state.
 *
 * 
 * @since 0.6
 *
 */
public interface StateRequest {

	/**
	 * The Variant session which created with this request by calling {@link Session#targetForState(State)}.
	 * 
	 * @return An object of type {@link Session}. Never null.
	 * @since 0.6
	 */
	public Session getSession();
	
	/**
	 * The target {@link State} of this request, which was passed to {@link Session#targetForState(State)}.
	 * 
	 * @return An object of type {@link State}. Never null.
	 * @since 0.6
	 */
	State getState();
	
	/**
	 * The current status of this state request.
	 * 
	 * @since 0.9
	 */
	Status getStatus();

	/**
	 * The state variant to which this state request resolved at run time, if any. A state request can either
	 * resolve to a {@link StateVariant}, or have trivial resolution, when all live experiences returned by
	 * {@link #getLiveExperiences()} are control experiences.
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
	 * resolved state parameters are the ones declared at the state level, if any. In the case of 
	 * non-trivial resolution, the parameters declared at the {@link StateVariant} level override the
	 * likely-named state parameters declared at the state level.
	 * 
	 * @return Immutable map keyed by parameter names. Never null, but may be empty.
     *
	 * @since 0.7
	 */	
	Map<String, String> getResolvedParameters();

	/**
	 * All live experiences in variations instrumented on this state.
	 * An experience is live if the containing variation is online and instrumented on this state, 
	 * and the session has been targeted for this experience.
	 * 
	 * @return Set of {@link Experience} object. Cannot be null, but may be empty.
	 *
	 * @since 0.6
	 */
	Set<Experience> getLiveExperiences();

	/**
	 * The live experience in a given variation, if any. See {@link #getLiveExperiences()} for
	 * definition of a live experience. 
	 * 
	 * @param variation {@link Variation} of interest.
	 * @return An {@link Optional}, containing the live experience
	 *         in the given variation, or empty if none.
	 * 
	 * @since 0.6
	 */
	Optional<Experience> getLiveExperience(Variation variation);

	/** Pending state visited event. State visited events are generated implicitly
	 *  whenever a new state request is created, and are triggered automatically at the time
	 *  the state request is committed with {@link #commit(Object...)} or failed with {@link #fail(Object...)}.
	 *  This method allows client code to enrich the pending state visited event with
	 *  extra attributes that will be useful for the downstream analysis.
	 *  
	 * @return Object of type {@link TraceEvent}. Cannot be null.
	 * @since 0.6
	 */
	TraceEvent getStateVisitedEvent();
		
	/**
	 * Complete this state request successfully. Causes the following to happen:
	 * <ul>
	 * <li> The associated state visited is triggered implicitly with the status of {@link StateRequest.Status#Committed}.
	 * <li> The associated session tracker's method {@link SessionIdTracker#save(Object...)} is
	 *      called.
	 * </ul>
     * 
     * <p>No-op if this request has already been committed in this or a parallel session.
	 * 
	 * @param userData   An array of zero or more opaque objects which will be passed to the {@link SessionIdTracker#save(Object...)}
	 * method without interpretation.
	 * 
	 * @since 0.6
	 */
	void commit(Object...userData);
	
	/**
	 * Complete this state request unsuccessfully. Causes the following to happen:
	 * <ul>
	 * <li> The associated state visited is triggered implicitly with the status of {@link StateRequest.Status#Failed}.
	 * <li> The associated session tracker's method {@link SessionIdTracker#save(Object...)} is
	 *      called.
	 * </ul>
     * 
     * <p>No-op if this request has already been committed in this or a parallel session.
	 * 
	 * @param userData   An array of zero or more opaque objects which will be passed to the {@link SessionIdTracker#save(Object...)}
	 * method without interpretation.
	 * 
	 * @since 0.6
	 */
	void fail(Object...userData);
	
	/**
	 * The current status of a state request. 
	 * @since 0.10
	 */
	public enum Status {

		/**
		 * Newly created state request. Must be either committed or failed before this session can call {@link Session#targetForState(State)} again.
		 * @since 0.10
		 */
		InProgress, 
		
		/**
		 * This state request has been completed successfully.
		 * @since 0.10
		 */
		Committed, 
		
		/**
		 * This state request has been completed abnormally.
		 * @since 0.10
		 */
		Failed;

	}	
}
