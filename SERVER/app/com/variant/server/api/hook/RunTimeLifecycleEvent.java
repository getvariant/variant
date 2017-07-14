package com.variant.server.api.hook;

import com.variant.core.LifecycleEvent;

/**
 * Marker super-interface for all life cycle event types, which are triggered at experiment's run time.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface RunTimeLifecycleEvent extends LifecycleEvent {}
