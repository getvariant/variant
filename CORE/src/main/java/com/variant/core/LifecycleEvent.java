package com.variant.core;

/**
 * Marker, ultimate super-interface for all life cycle event types. Concrete implementations
 * are made available to client code via the {@code UserHook.post(LifecycleEvent)} callback.
 * Any concrete life cycle event class will provide the default hook, which will be posted
 * if no user hooks were defined or none returned a value.
 *
 * User hooks provide a consistent way of extending the functionality of Variant server 
 * with custom semantics. They are custom listeners which can augment Variant functionality
 * with application specific semantics, injected at predefined points the lifecycle of Variant objects.
 *  
 * @author Igor Urisman.
 * @since 0.5
 */

public interface LifecycleEvent {}