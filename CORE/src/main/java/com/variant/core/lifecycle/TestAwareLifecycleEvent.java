package com.variant.core.lifecycle;

import com.variant.core.schema.Test;

/**
 * Super-type of all life-cycle event types whose runtime context includes a particular test.
 * 
 * @since 0.7
 *
 */
public interface TestAwareLifecycleEvent extends LifecycleEvent{

	/**
	 * The event's triggering test.
	 * 
	 * @return An object of type {@link Test}.
	 * 
     * @since 0.7
	 */
	public Test getTest();
	
}
