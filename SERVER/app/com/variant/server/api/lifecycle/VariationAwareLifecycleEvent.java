package com.variant.server.api.lifecycle;

import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.schema.Variation;

/**
 * Super-type of all life-cycle event types whose runtime context includes a particular experience variation.
 * 
 * @since 0.7
 *
 */
public interface VariationAwareLifecycleEvent extends LifecycleEvent{

	/**
	 * The event's triggering test.
	 * 
	 * @return An object of type {@link Variation}.
	 * 
     * @since 0.7
	 */
	public Variation getVariation();
	
}
