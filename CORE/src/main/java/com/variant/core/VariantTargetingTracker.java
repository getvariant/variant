package com.variant.core;

import java.util.Collection;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * <p>A container-initialized implementation will use external mechanisms to obtain and to store
 * the current session's targeting information between state requests. For instance, 
 * in a web application environment, this may be tracked in an HTTP cookie. Because an implementation
 * must be instantiated by the container, it must provide a no-argument constructor.
 * 
 * @author Igor Urisman
 * @since 0.5	 
 */

public interface VariantTargetingTracker {
	
	/**
	 * <p>The container will call this method immediately following the instantiation to allow
	 * the client code to initialize the object with some state.
	 * 
	 * @param api Current Variant Core API handle.
	 * @param session Current Variant session.
	 * @param userData An array of 0 or more opaque objects which 
	 *                 {@link com.variant.core.Variant#targetSession(VariantSession, com.variant.core.schema.State, Object...)} 
	 *                 will pass here without interpretation.
	 * @since 0.5
	 */
	public void initialized(Variant core, VariantSession session, Object...userData);
	
	/**
	 * All currently tracked test experiences. The implementation must guarantee 
	 * consistency of this operation, e.g. that all experiences are pairwise independent,
	 * i.e. there are no two experiences in the returned collection that belong to the same test.
	 * 
	 * @return Collection of objects of type {@link com.variant.core.schema.Test.Experience}.
	 * @since 0.5
	 */
	public Collection<Experience> getAll();
	
	/**
	 * Currently tracked test experience from a given test, if any.
	 * 
	 * @param test Test of interest.
	 * @return Given tests's experience that is currently tracked by this tracker, or null if none.
	 * @since 0.5
	 */
	public Experience get(Test test);
		
	/**
	 * Remove the currently tracked test experience from a given test, if any.
	 * 
	 * @param test Test of interest.
	 * @return Given tests's experience that was removed form this tracker by this operation, oif any.
	 * @since 0.5
	 */
	public Experience remove(Test test);

	/**
	 * Add a test experience to this tracker.
	 * 
	 * @param experience Experience to be added.
	 * @param timestamp The timestamp. 
	 * @since 0.5
	 */
	public Experience add(Experience experience, long timestamp);
	
	/**
	 * Update the timestamp of a given tests's entry, if any.
	 *
	 * @param test Test of interest.
	 * @since 0.5
	 */
	public void touch(Test test);
	
	/**
	 * Flush the state of this object to the underlying srorage.
	 * 
	 * @param userData An array of 0 or more opaque objects which 
	 *                 {@link com.variant.core.Variant#commitStateRequest(VariantStateRequest, Object...)} 
	 *                 will pass here without interpretation.
	 */
	public void save(Object...userData);
		
}
