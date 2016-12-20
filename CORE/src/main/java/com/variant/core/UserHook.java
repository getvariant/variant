package com.variant.core;

/**
 * Marker, ultimate super-interface for all user hook types. Concrete implementations
 * are made available to host code via the {@link HookListener#post(UserHook)} callback.
 * Any concrete user hook class will implement some sub-interface of this.
 *
 * User hooks provide a consistent way of extending the functionality of Variant server 
 * with custom semantics. They are predefined points in the execution path, to which the 
 * application developer may attach a callback method to be invoked by the server whenever 
 * that point is reached. To attach a custom callback method to a user hook, application 
 * programmer must register a hook listener by passing it to <code>VariantClient.addHookListener(HookListener)</code>.
 *  
 * @author Igor Urisman.
 * @since 0.5
 */

public interface UserHook {}