package com.variant.server.api.lifecycle;

import com.variant.core.schema.State;

/**
 * Super-type of all lifecycle event types, whose runtime context includes a particular state.
 * 
 * @since 0.7
 *
 */
public interface StateAwareLifecycleEvent extends LifecycleEvent {

	/**
	 * The schema state, associated with this event.
	 * 
	 * @return An object of type {@link State}.
	 * 
     * @since 0.7
	 */
	public State getState();
	
}
