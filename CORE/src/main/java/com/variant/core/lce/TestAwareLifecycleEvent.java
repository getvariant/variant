package com.variant.core.lce;

import com.variant.core.schema.Test;

/**
 * <p>p>Super-interface for all life cycle event types, whose scope is limited to a particular test.
 * 
 * @author Igor Urisman.
 * @since 0.7
 *
 */
public interface TestAwareLifecycleEvent extends RuntimeLifecycleEvent{

	/**
	 * The event's triggering test.
	 * 
	 * @return An object of type {@link Test}.
	 * 
     * @since 0.7
	 */
	public Test getTest();
	
}
