package com.variant.core.hook;

/**
 * Marker, ultimate super-interface for all user hook types. Concrete implementations
 * are made available to host code via the {@link HookListener#post(UserHook)} callback.
 * Any concrete user hook class will implement some sub-interface of this.
 * 
 * @author Igor Urisman.
 * @since 0.5
 */

public interface UserHook {}