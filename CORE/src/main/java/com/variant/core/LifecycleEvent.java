package com.variant.core;


/**
 * <p>Ultimate super-type of all lifecycle event types. Concrete implementations
 * are made available to user code via the {@link LifecycleHook#post(LifecycleEvent)} callback.
 *
 * <p>Lifecycle hooks provide a consistent way of extending the functionality of Variant server 
 * and client with custom semantics via callback methods, which can augment defaultt functionality
 * with application specific semantics.
 *  
 * @since 0.5
 */

public interface LifecycleEvent {
	
	/**
	 * <p>Make a new object of the right type, suitable as the return type of Implementations of the
	 * {@link LifecycleHook#post(LifecycleEvent)} method. Concrete lifecycle events will override this
	 * with a suitable narrower return type.
	 *  
	 * @since 0.10
	 */
	public PostResult newPostResult();
	
	/**
	 * The result of the {@link LifecycleHook#post(LifecycleEvent)} callback. Concrete implementations will have methods
	 * for the user code to set the details of the outcome of the post operation expected by the server. Call the
	 * appropriate method of {@code com.variant.server.api.lifecycle.PostResultFactory} to obtain a post result to be
	 * returned by your code.
	 * 
	 * @since 0.7
	 */
	public interface PostResult {}

}