package com.variant.client;

import java.util.Collection;

import com.variant.core.schema.Test.Experience;

/**
 * <p>An environment dependent implementation will use some external mechanism to obtain and to store
 * customer's targeting information between Variant sessions, thus providing experiment scoped
 * targeting stability. In a web application environment, this may be tracked in an HTTP cookie,
 * which would provide "weak" targeting stability, i.e. ensure that the return user sees the same
 * experiences, so long as he's using the same Web browser and that cookies have not been removed
 * since the last visit. 
 * 
 * <p>Stronger experience stability could be provided via the operational database, 
 * which won't depend on the user's environment, so long as the user can be reliably identified.
 * 
 * <p>By contract, an implementation must provide a no-argument constructor, which Variant will use
 * to instantiate it. To inject state, call {@link #init(Session, Object...)}. An implementation
 * object will remain in effect for the duration of a Variant session. But since its state may change
 * by a state request, Variant asks it to save itself to external storage by calling 
 * {@link #save(Object...)}, within the scope of the {@link StateRequest#commit(Object...)} method.
 * 
 * @author Igor Urisman
 * @since 0.6
 */

public interface TargetingTracker {
	
	/**
	 * <p>Called by Variant to initialize a newly instantiated concrete implementation 
	 * immediately following the instantiation. Called within the scope of the {@code Connection.getSession()} methods.
	 * 
	 * @param conn      The Variant server connection which is initializing this object.
	 * @param userData  An array of zero or more opaque objects, which the enclosing call to {@link Connection#getSession(Object...) }
	 *                  or {@link Connection#getOrCreateSession(Object...)} will pass here without interpretation. 
	 * 
	 * @since 0.6
	 */
	public void init(Session connection, Object...userData);

	
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
	 * Sets the test experiences tracked by this object. . Call {@link #save(Object...)} to persist between
	 * state request.
	 * 
	 * @param entries Collection of objects of type {@link Entry}. The caller must guarantee 
	 *                consistency of this collection, i.e. that all entries are pairwise independent,
	 *                which is to say that there be no two entries which refer to the same test.
	 * 
	 * @since 0.6
	 */
	public void set(Collection<Entry> entries);

	/**
	 * Saves the state of this object to the underlying persistence mechanism, making it durable between
	 * Variant sessions. Called by Variant within the scope of the {@link StateRequest#commit(Object...)} method. 
	 * 
	 * @param userData An array of zero or more opaque objects which {@link StateRequest#commit(Object...)}
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
		 * Get test experience tracked by this entry as an instance {@link Experience}.
		 * 
		 * @return Test experience from the underlying connection's schema, whose name and test name match the content
		 *         of this entry, or null if current schema does not have such an experience.
		 *         
    	 * @since 0.6
		 */
		public Experience getAsExperience();
		
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
