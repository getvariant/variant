package com.variant.core.schema;

import com.variant.core.LifecycleEvent;

/**
 * <p>Super-interface for all life cycle event types that post their hooks at schema parse time.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface ParseTimeLifecycleEvent extends LifecycleEvent {}
