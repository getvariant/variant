package com.variant.client;

import java.util.HashSet;
import java.util.Set;

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
 * use {@link #init(Object...)}.
 * <p>
 * Configured by <code>targeting.tracker.class.name</code> configuration property.
 *
 * @since 0.6
 */

public interface TargetingTracker {
	
	/**
	 * Initialize this tracker to reflect the state contained in external persistence mechanism.
	 * Called by Variant to initialize a newly instantiated concrete implementation, 
	 * within the scope of {@link Connection#getSession(Object...)}
	 * or {@link Connection#getOrCreateSession(Session, Object...)} methods.
	 * 
	 * @param userData  An array of zero or more opaque objects, which the enclosing call to {@link Connection#getSession(Object...) }
	 *                  or {@link Connection#getOrCreateSession(Object...)} will pass here without interpretation. 
	 * 
	 * @since 0.6
	 */
	void init(Object...userData);

	/**
	 * All currently tracked experiences. The implementation must guarantee 
	 * consistency of this operation, i.e. that the returned set does not contain two entries
	 * both referring to the same test.
	 * 
	 * @return Set of zero or more of objects of type {@link Entry} each corresponding to
	 *         a test experience currently tracked by this object. Must never return null.
	 * @since 0.6
	 */
	Set<Entry> get();
		
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
	void set(Set<Entry> entries);

	/**
	 * Saves the state of this object to the external persistence mechanism, making it durable between
	 * Variant sessions. Called by Variant within the scope of the {@link StateRequest#commit(Object...)} method. 
	 * 
	 * @param userData An array of zero or more opaque objects which {@link StateRequest#commit(Object...)}
	 *                 will pass here without interpretation.
	 *                 
	 * @since 0.6
	 */
	void save(Object...userData);
		
	/**
	 * A targeting tracker entry.
	 * Encapsulates the test experience and the timestamp of when this experience was last seen by a user.
	 * 
	 * @since 0.6
	 */
	public static interface Entry {
				
		/**
		 * Name of the test.
		 * 
		 * @return Test name.
		 *         
    	 * @since 0.6
		 */
		public String getTestName();
		
		/**
		 * Name of the experience.
		 * 
		 * @return Experience name.
		 *         
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

	/**
	 * An empty targeting tracker.
	 * 
	 * @since 0.9
	 * @return
	 */
	public static TargetingTracker empty() {
		
		return new TargetingTracker() {

			@Override
			public void init(Object... userData) {}

			@Override
			public Set<Entry> get() {
				return new HashSet<Entry>();
			}

			@Override
			public void set(Set<Entry> entries) {}

			@Override
			public void save(Object... userData) {}
			
		};
	}
}
