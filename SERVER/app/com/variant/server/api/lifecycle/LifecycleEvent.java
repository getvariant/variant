package com.variant.server.api.lifecycle;

import com.variant.server.api.Session;

/**
 * <p>Ultimate super-type of all lifecycle event types. Concrete implementations
 * are made available to user code via the {@link LifecycleHook#post(LifecycleEvent)} callback method.
 * Lifecycle hooks provide a consistent way of extending the functionality of Variant server 
 * with custom semantics via callback methods, which can augment defaultt functionality
 * with application specific semantics.
 *  
 * @since 0.5
 */

public interface LifecycleEvent {
	
	/**
	 * User session on whose behalf the event is raised.
	 * @return
	 * @since 0.8
	 */
	Session getSession();

	/**
	 * <p>Make a new object of the right type, suitable to be returned by the
	 * {@link LifecycleHook#post(LifecycleEvent)} method.
	 *  
	 * @since 0.10
	 */
	public PostResult mkPostResult();
	
	/**
	 * The default lifecycle hook, which is automatically appended to each life cycle event's hook chain.
	 * It is posted in case either 1) no lifecycle hooks were defined in the schema, or 2) none of those
	 * lifecycle hooks defined in the schema for this event type returned a non-empty result.
	 * 
	 *  @since 0.7
	 */
	LifecycleHook<? extends LifecycleEvent> getDefaultHook();
	
	/**
	 * The return type of the {@link LifecycleHook#post(LifecycleEvent)} callback method. Concrete implementations will have methods
	 * for the user code to set the details of the outcome of the post operation expected by the server. Call the
	 * appropriate method of {@code com.variant.server.api.lifecycle.PostResultFactory} to obtain a post result to be
	 * returned by your code.
	 * 
	 * @since 0.7
	 */
	public interface PostResult {}

}