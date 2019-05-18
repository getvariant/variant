package com.variant.server.api.lifecycle;

import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.schema.State;

/**
 * Super-type of all life-cycle event types whose runtime context includes a particular state.
 * 
 * @since 0.7
 *
 */
public interface StateAwareLifecycleEvent extends LifecycleEvent {

	/**
	 * The event's triggering state.
	 * 
	 * @return An object of type {@link State}.
	 * 
     * @since 0.7
	 */
	public State getState();
	
}
