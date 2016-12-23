package com.variant.client;

import java.util.Collection;

import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.Experience;

/**
 * <p>An environment dependent implementation will use external mechanisms to obtain and to store
 * customer's targeting information between Variant sessions, thus providing experiment scoped
 * targeting stability. In a web application environment, this may be tracked in an HTTP cookie,
 * which would provide "weak" targeting stability (the cookie could be deleted between sessions), 
 * or, more likely, in the operational database, which could support "strong" stability across
 * different devices.
 * 
 * <p>By contract, an implementation must provide a no-argument constructor, which Variant will use
 * to instantiate it. To inject state, call {@link #init(VariantInitParams, Object...)}.
 * 
 * @author Igor Urisman
 * @since 0.6
 */

public interface VariantTargetingTracker {
	
	/**
	 * <p>Called by Variant to initialize a newly instantiated concrete implementation. Variant client calls this method 
	 * immediately following the instantiation within the scope of the {@link VariantClient#getSession(Object...)} method.
	 * Use this to inject state from configuration.
	 * 
	 * @param conn      The Variant server connection which is initializing this object.
	 * @param userData  An array of zero or more opaque objects which {@link VariantClient#getSession(Object...)}  
	 *                  or {@link VariantClient#getOrCreateSession(Object...)} method will pass here without 
	 *                  interpretation.
	 * 
	 * @since 0.6
	 */
	public void init(Connection connection, Object...userData);

	
	/**
	 * All currently tracked test experiences. The implementation must guarantee 
	 * consistency of this operation, i.e. that all returned experiences are pairwise independent,
	 * which is to say that there be no two experiences which refer to the same test.
	 * 
	 * @return Collection of zero or more of objects of type {@link Entry} each corresponding to
	 *         a test experience currently tracked by this object.
	 * @since 0.6
	 */
	public Collection<Entry> get();
		
	/**
	 * Set the value of all currently tracked test experiences.
	 * 
	 * @param entries Collection of objects of type {@link Entry}. The caller must guarantee 
	 *                consistency of this collection, i.e. that all entries are pairwise independent,
	 *                which is to say that there be no two entries which refer to the same test.
	 * 
	 * @since 0.6
	 */
	public void set(Collection<Entry> entries);

	/**
	 * Called by Variant to save the state of this object to the underlying persistence mechanism.
	 * 
	 * @param userData An array of zero or more opaque objects which {@link VariantCoreStateRequest#commit(Object...)}
	 *                 will pass here without interpretation.
	 *                 
	 * @since 0.6
	 */
	public void save(Object...userData);
		
	/**
	 * A targeting tracker entry.
	 * Encapsulates the test name, experience name and the timestamp of when this experience was last seen by a user.
	 * 
	 * @since 0.6
	 */
	public static interface Entry {
				
		/**
		 * Get test experience trakced by this entry as an instance {@link Experience} with respect to an experiment schema.
		 * 
		 * @param schema Variant {@link Schema} (presumably current) where the experience will be looked for.
		 * 
		 * @return Test experience from the given schema whose name and test name match the content
		 *         of this entry, or null if current schema does not have such an experience.
		 *         
    	 * @since 0.6
		 */
		public Experience getAsExperience(Schema schema);
		
		/**
		 * Get the test name, tracked by this entry.
		 * 
		 * @return Test name.
    	 * @since 0.6
		 */
		public String getTestName();
		
		/**
		 * Get the experience name tracked by this entry.
		 * 
		 * @return Experience name.
    	 * @since 0.6
		 */
		public String getExperienceName();
		
		/**
		 * Get the timestamp when this experience was last seen by a user.
		 * @return
    	 * @since 0.6
		 */
		public long getTimestamp();
	}

}
