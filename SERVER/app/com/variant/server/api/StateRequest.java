package com.variant.server.api;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.variant.core.StateRequestStatus;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;

/**
 * The server side of a Variant state request. Contains state request-scoped application 
 * state. Not directly mutable by the client code, i.e. all methods are read-only.
 *
 * @author Igor Urisman
 * @since 0.7
 *
 */

public interface StateRequest {

	/**
	 * Foreground session to which this state request belongs.
	 * 
	 * @return An object of type {@link Session}.
	 * @since 0.7
	 */
	Session getSession();

	/**
	 * The target of this state request.
	 * 
	 * @return An object of type {@link State}.
	 * @since 0.7
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
	 * <p>Targeted experiences in variations instrumented on the target state.
	 * An experience is live iff a) this session has been targeted for it, b) its containing variation
	 * is instrumented on this state, and c) is neither off nor disqualified in this session.
	 * 
	 * @return A {@link Set} of {@link Test.Experience} objects.
	 * @since 0.7
	 */
	Set<Experience> getLiveExperiences();

	/**
	 * The live experience in a given variation, if any. See {@link #getLiveExperiences()} for
	 * definition of live experience. Returns an empty {@link Optional} if the
	 * target state of this request is not instrumented by the given variation. 
	 * 
	 * @param variation {@link Variation}
	 * 
	 * @return An {@link Optional}, containing the live experience, if session has been targeted
	 *         for the given variation, or empty otherwise.
	 * 
	 * @since 0.7
	 */
	Optional<Experience> getLiveExperience(Variation variation);

	/**
	 * The {@link StateVariant} to which this state request resolved.
	 * 
	 * @return The {@link StateVariant} to which this request resolved.
	 * 
	 * @since 0.7
	 * @see StateVariant
     */
	public StateVariant getResolvedStateVariant();
	
	/**
	 * The resolved state parameters as an immutable map, containing the merged maps of the state
	 * parameters defined at the state level and at the state variant level, with the latter taking
	 * precedence over former.  
	 * 
	 * @return Immutable map keyed by parameter names.
	 * @since 0.7
	 */	
	public  Map<String,String> getResolvedParameters();

}
