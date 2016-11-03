package com.variant.core;

import java.util.Collection;
import java.util.Set;

import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * Variant Core state request. CLEANUP
 * 
 * @author Igor Urisman
 * @since 0.6
 *
 *
public interface VariantCoreStateRequest {

	/**
	 * The Variant session that obtained this request via {@link VariantCoreSession#targetForState(State)}.
	 * 
	 * @return An object of type {@link VariantCoreSession}.
	 * @since 0.6
	 *
	VariantCoreSession getSession();
	
	/**
	 * The {@link State} of this request, which was passed to {@link VariantCoreSession#targetForState(State)}.
	 * 
	 * @return An object of type {@link State}
	 * @since 0.6
	 *
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
	 *
	StateVariant getResolvedStateVariant();
		
	/**
	 * The resolved state parameter. In case of trivial resolution, resolved state parameters are the ones declared at the
	 * state level. In the case of non-trivial resolution, the parameters declared at the {@link StateVariant} level override
	 * likely-named state parameterts declared at the state level.  
	 * 
	 * @param name Case insensitive parameter name.
	 * @return Resolved parameter value or null if no parameter with this name defined.
	 * @since 0.6
	 *
	String getResolvedParameter(String name);

	/**
	 * The names of all resolved state parameters. In case of trivial resolution, resolved state parameters are the ones declared at the
	 * state level. In the case of non-trivial resolution, the parameters declared at the {@link StateVariant} level override
	 * likely-named state parameterts declared at the state level.  
	 * 
	 * @return An unmodifiable collection of all resolved state parameters.
	 * @since 0.6
	 *	
	Set<String> getResolvedParameterNames();

	/**
	 * <p>This session's all live experiences on this state.
	 * An experience is live iff this session has been targeted for it and its containing test
	 * is instrumented on this state and is neither off nor disqualified in this session.
	 * Both, variantful and non-variant instrumentations are included.
	 * 
	 * @return Collection of {@link Test.Experience}s.
	 * @since 0.6
	 *
	Collection<Experience> getLiveExperiences();

	/**
	 * The live experience in a given test, if any. See {@link #getLiveExperiences()} for
	 * definition of live experience.
	 * 
	 * @param test {@link Test}
	 * @return An object of type {@link Experience}. 
	 * 
	 * @since 0.6
	 *
	Experience getLiveExperience(Test test);
		
	/** Pending state visited event. 
	 *  This is useful if the caller wants to add parameters to this event before it is flushed to external storage.
	 *  
	 * @return Object of type {@link VariantEvent}.
	 * @since 0.6
	 *
	VariantEvent getStateVisitedEvent();
	
	/**
	 * Set the status of this request.
	 * 
	 * @param status {@link Status}
	 *
	void setStatus(Status status);
	
	/**
	 * Commit this state request.
     * The pending state visited {@link VariantEvent} is triggered.
     *
	 * @since 0.6
	 *
	void commit();

	/**
	 * Is this request object represent a request that has been committed?
     * 
     *@return true if this request has ben committed, or false otherwise.
	 * @since 0.6
	 *
	boolean isCommitted();

	/**
	 * Current status of this request.
	 *
	Status getStatus();

	/**
	 * Status of a {@link com.variant.core.VariantCoreStateRequest}.
	 *
	static enum Status {
		OK, FAIL
	}
}
*/