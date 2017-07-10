package com.variant.server.api.hook;

import com.variant.core.LifecycleEvent;

/**
 * Marker super-interface for all life cycle event types, which post their hooks at experiment run time.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface RunTimeLifecycleEvent extends LifecycleEvent {}
