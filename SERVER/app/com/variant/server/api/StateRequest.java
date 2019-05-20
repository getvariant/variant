package com.variant.server.api;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;

/**
 * State request, as returned by {@link Session#getStateRequest()}. Cannot be directly manipulated by the client code, 
 * i.e. all methods are read-only.
 *
 * @author Igor Urisman
 * @since 0.7
 *
 */

public interface StateRequest {

	/**
	 * User session to which this state request belongs.
	 * 
	 * @return An object of type {@link Session}. Cannot be null.
	 * @since 0.7
	 */
	Session getSession();

	/**
	 * The target state of this state request.
	 * 
	 * @return An object of type {@link State}. Cannot be null.
	 * @since 0.7
	 */
	State getState();

	/**
	 * The current status of this state request.
     * 
     * @return An object of type {@link Status}. Cannot be null.
	 * 
	 * @since 0.9
	 */
	Status getStatus();

	/**
	 * <p>Targeted experiences in variations instrumented on the target state.
	 * An experience is live iff a) this session has been targeted for it, b) its containing variation
	 * is instrumented on this state, and c) is neither off nor disqualified in this session.
	 * 
	 * @return A {@link Set} of {@link Variation.Experience} objects. Cannot be null, but may be empty.
	 * @since 0.7
	 */
	Set<Experience> getLiveExperiences();

	/**
	 * The live experience in a given variation, if any.  
	 * 
	 * @param variation {@link Variation}
	 * 
	 * @return An {@link Optional}, containing the live experience, if a live experience in the given
	 *         variation exists, or empty otherwise.
	 * 
	 * @since 0.7
	 */
	Optional<Experience> getLiveExperience(Variation variation);

	/**
	 * The state variant to which this state request resolved at run time. A state request can 
	 * have either trivial resolution, or resolve to a {@link StateVariant}. Trivial resolution means that all live
	 * experiences are control experiences, and the user session will be targeted for the base state. If at least one
	 * live experience is a variant, the targeting operation will resolve to some state variant.
	 * 
	 * @return The {@link Optional} containing the {@link StateVariant} object to which this request resolved, 
	 *         or empty if all live experiences are control.
	 * 
	 * @since 0.7
	 * @see StateVariant
	 */
	Optional<StateVariant> getResolvedStateVariant();
	
	/**
	 * The resolved state parameters as an immutable map, containing the merged maps of the state
	 * parameters defined at the state level and at the state variant level, with the latter taking
	 * precedence over former.  
	 * 
	 * @return Immutable map keyed by parameter names.
	 * @since 0.7
	 */	
	Map<String,String> getResolvedParameters();

	/**
	 * 
	 * @since 0.10
	 */
	public enum Status {

		InProgress, Committed, Failed;

		/**
		 * Is this value one of the given values?
		 * 
		 * @param statuses
		 * @return ture if this value is one of the given values, false otherwise.
		 */
		public boolean isIn(Status... statuses) {
			
			for (Status s: statuses) if (this == s) return true;
			return false;
		}
	}
}
