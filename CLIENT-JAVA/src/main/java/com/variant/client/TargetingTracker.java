package com.variant.client;

import java.util.Set;

import com.variant.core.schema.Test.Experience;

/**
 * Interface to be implemented by an environment-bound targeting tracker. The implementation will 
 * use an external mechanism to provide durable targeting, i.e. to store the current user's
 * targeting information between sessions. For a recognized user, reliable durable targeting can
 * be achieved by storing user's targeting info in the database. If the user is anonymous,
 * his targeting information may be stored in the client's local durable storage, e.g. an HTTP 
 * cookie, but this approach is not reliable as the user may return on a different device.
 * <p>
 * The implementation will have request scoped life-cycle, i.e. Variant will re-instantiate the 
 * implementing class at the start of each state request.
 * <p>
 * By contract, an implementation must provide a no-argument constructor. To inject initial state, 
 * use {@link #init(Session, Object...)}.
 * <p>
 * Configured by <code>targeting.tracker.class.name</code> configuration property.
 *
 * @since 0.6
 */

public interface TargetingTracker {
	
	/**
	 * <p>Called by Variant to initialize a newly instantiated concrete implementation, 
	 * within the scope of {@link Connection#getSession(Object...)}
	 * or {@link Connection#getOrCreateSession(Object...)} methods.
	 * 
	 * @param session   The Variant session initializing this object.
	 * @param userData  An array of zero or more opaque objects, which the enclosing call to {@link Connection#getSession(Object...) }
	 *                  or {@link Connection#getOrCreateSession(Object...)} will pass here without interpretation. 
	 * 
	 * @since 0.6
	 */
	public void init(Session session, Object...userData);

	
	/**
	 * All currently tracked experiences. The implementation must guarantee 
	 * consistency of this operation, i.e. that the returned set does not contain two entries
	 * both referring to the same test.
	 * 
	 * @return Set of zero or more of objects of type {@link Entry} each corresponding to
	 *         a test experience currently tracked by this object.
	 * @since 0.6
	 */
	public Set<Entry> get();
		
	/**
	 * Sets the test experiences tracked by this object. Call {@link #save(Object...)} to persist between
	 * state request.
	 * 
	 * @param entries Set of objects of type {@link Entry}. The caller must guarantee 
	 *                consistency of this operation, i.e. that the returned set does not contain two entries
	 *                both referring to the same test.
	 * 
	 * @since 0.6
	 */
	public void set(Set<Entry> entries);

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
