package com.variant.server.api;

import com.variant.core.LifecycleEvent;

/**
 * <p>Super-interface for all life cycle event types that post their hooks at experiment run time.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface RunTimeLifecycleEvent extends LifecycleEvent {}
