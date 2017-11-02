package com.variant.core.lce;

import com.variant.core.schema.State;

/**
 * <p>Super-interface for all life cycle event types, whose scope is limited to a particular state.
 * 
 * @author Igor Urisman.
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
