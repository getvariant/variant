package com.variant.core.lifecycle;


/**
 * <p>Ultimate super-type of all life-cycle event types. Concrete implementations
 * are made available to user code via the {@link LifecycleHook#post(LifecycleEvent)} callback.
 *
 * <p>Lifecycle hooks provide a consistent way of extending the functionality of Variant server 
 * and client with custom semantics via callback methods, which can augment defaultt functionality
 * with application specific semantics.
 *  
 * @author Igor Urisman.
 * @since 0.5
 */

public interface LifecycleEvent {
	
	/**
	 * The default life-cycle hook, which is automatically added to each life cycle event's hook chain.
	 * It is posted in case either 1) no life-cycle hooks were defined in the schema, or 2) none of those
	 * life-cycle hooks defined in the schema for this event type returned a non-null response.
	 * 
	 *  @since 0.7
	 */
	public LifecycleHook<? extends LifecycleEvent> getDefaultHook();
}