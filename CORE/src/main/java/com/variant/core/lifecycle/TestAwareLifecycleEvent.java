package com.variant.core.lifecycle;

import com.variant.core.schema.Variation;

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
	 * @return An object of type {@link Variation}.
	 * 
     * @since 0.7
	 */
	public Variation getTest();
	
}
