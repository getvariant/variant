package com.variant.core.lifecycle;


/**
 * <p>Ultimate super-type of all life-cycle event types. Concrete implementations
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
	public LifecycleHook.PostResult newPostResult();
}