package com.variant.client;

import java.util.Collection;

import com.variant.core.VariantStateRequest;
import com.variant.core.schema.Test.Experience;

/**
 * <p>A Variant-instantiated implementation will use external mechanisms to obtain and to store
 * customer's targeting information between Variant sessions, thus implementing experiment scoped
 * targeting stability. In a web application environment, this may be tracked in an HTTP cookie,
 * which would provide "weak" experiment scoped targeting stability (the cookie could be deleted
 * between sessions), or, more likely, in the client's database, which would be "strong," so long
 * as the returning customer can be recognized.
 * 
 * @author Igor Urisman
 * @since 0.6
 */

public interface VariantTargetingTracker {
	
	/**
	 * <p>Called by Variant client immediately following the instantiation, within the scope of the
	 * {@link VariantClient#getSession(Object...userData)} method.
	 * 
	 * @param initParams The init parameter map, as specified by the <code>targeting.tracker.class.init</code>
	 *                   application property. 
	 * @param client     Variant client that instantiated this tracker.
	 * @param userData   An array of 0 or more opaque objects which {@link VariantClient#getSession(Object...userData)}  
	 *                   will pass here without interpretation.
	 * 
	 * @since 0.6
	 */
	public void initialized(VariantInitParams initParams, Object...userData) throws Exception;

	
	/**
	 * All currently tracked test experiences. The implementation must guarantee 
	 * consistency of this operation, e.g. that all experiences are pairwise independent,
	 * i.e. there are no two experiences in the returned collection that belong to the same test.
	 * 
	 * @return Collection of objects of type {@link Entry}.
	 * @since 0.6
	 */
	public Collection<Entry> get();
		
	/**
	 * Set the value of all currently tracked test experiences. The implementation must guarantee 
	 * consistency of this operation, e.g. that all experiences are pairwise independent,
	 * i.e. there are no two experiences in the returned collection that belong to the same test.
	 * 
	 * @param entries Collection of objects of type {@link Entry}.
	 * 
	 * @since 0.6
	 */
	public void set(Collection<Entry> entries);

	/**
	 * Flush the state of this object to the underlying persistence mechanism.
	 * 
	 * @param userData An array of 0 or more opaque objects which 
	 *                 {@link com.variant.core.Variant#commitStateRequest(VariantStateRequest, Object...)} 
	 *                 will pass here without interpretation.
	 *                 
	 * @since 0.6
	 */
	public void save(Object...userData);
		
	/**
	 * A targeting tracker entry: a test experience plus the timestamp of when it was last touched by this
	 * front end.
	 */
	public static interface Entry {
				
		/**
		 * Test Experience from targeting tracker.
		 * @return
		 */
		public Experience getExperience();
		
		/**
		 * Timestamp a state instrumented by this test has been last touched.
		 * @return
		 */
		public long getTimestamp();
	}

}
