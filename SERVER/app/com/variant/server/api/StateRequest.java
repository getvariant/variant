package com.variant.server.api;

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
	 * Creation timestamp.
     * 
     *@return Creation timestamp of this state request object.
	 * @since 0.7
	 */
	Date createDate();

	/**
	 * Has this state request been committed?
     * 
     *@return true if this request has ben committed, or false otherwise.
	 * @since 0.7
	 */
	boolean isCommitted();

	/** Pending state visited event. 
	 *  This is useful if the caller wants to add parameters to this event before it is triggered.
	 *  
	 * @return Object of type {@link VariantEvent}.
	 * @since 0.7
	 */
	VariantEvent getStateVisitedEvent();

	/**
	 * Current status of this request.
	 * 
	 * @return Status of this request.
	 * @since 0.7
	 */
	StateRequestStatus getStatus();

	/**
	 * <p>Targeted test experiences in tests instrumented on this state.
	 * An experience is live iff this session has been targeted for it and its containing test
	 * is instrumented on this state and is neither off nor disqualified in this session.
	 * Both, variant and non-variant instrumentations are included.
	 * 
	 * @return Collection of {@link Test.Experience}s.
	 * @since 0.7
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
	 * @since 0.7
	 */
	Experience getLiveExperience(Test test);

	/**
	 * The state variant to which this state request resolved at run time. A state request can 
	 * have either trivial resolution, or resolve to a {@link StateVariant}. Trivial resolution means that all live
	 * experiences are control experiences, and the user session will be targeted for the base state. If at least one
	 * live experience is a variant, the targeting operation will resolve to some state variant definition in the schema.
	 * 
	 * @return The {@link StateVariant} to which this request resolved, or null if all live experiences are control.
	 * 
	 * @since 0.7
	 * @see StateVariant
     */
	public StateVariant getResolvedStateVariant();
	
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
	public  Map<String,String> getResolvedParameters();

}
