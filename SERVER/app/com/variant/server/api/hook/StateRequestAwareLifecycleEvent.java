package com.variant.server.api.hook;

import com.variant.server.api.StateRequest;



/**
 * <p>Super-interface for all life cycle event types with the runtime scope of request.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface StateRequestAwareLifecycleEvent extends RunTimeLifecycleEvent {

	/**
	 * Client code can obtain the state request in progress by calling this method.
	 * 
	 * @return An object of type {@link StateRequest}.
	 * 
     * @since 0.7
	 */
	public StateRequest getStateRequest();

}
