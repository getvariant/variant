package com.variant.core;

/**
 * <p>Ultimate super-interface for all life cycle event types. Concrete implementations
 * are made available to client code via the {@link UserHook#post(LifecycleEvent)} callback.
 *
 * <p>User hooks provide a consistent way of extending the functionality of Variant server 
 * with custom semantics. They are custom callback functions, which can augment Variant functionality
 * with application specific semantics, injected at predefined points the life cycle of Variant 
 * objects.
 *  
 * @author Igor Urisman.
 * @since 0.5
 */

public interface LifecycleEvent {
	
	/**
	 * The default user hook, which is automatically added to each life cycle event's hook chain.
	 * It is posted in case either 1) no user hooks were defined in the schema, or 2) none of those
	 * user hooks defined in the schema for this event type returned a non-null response.
	 * 
	 *  @since 0.7
	 */
	public UserHook<? extends LifecycleEvent> getDefaultHook();
}