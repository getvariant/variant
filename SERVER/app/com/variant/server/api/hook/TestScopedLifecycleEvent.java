package com.variant.server.api.hook;

import com.variant.core.schema.Test;
import com.variant.server.api.StateRequest;

/**
 * <p>Super-interface for all run time life cycle event types .
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface TestScopedLifecycleEvent extends StateRequestAwareLifecycleEvent{

	/**
	 * Client code can obtain the state request in progress by calling this method.
	 * 
	 * @return An object of type {@link StateRequest}.
	 * 
     * @since 0.7
	 */
	public Test getTest();
	
}
