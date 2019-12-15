package com.variant.server.api.lifecycle;

import com.variant.share.schema.Variation;

/**
 * Super-type of all lifecycle event types, whose runtime context includes a particular variation.
 * 
 * @since 0.7
 *
 */
public interface VariationAwareLifecycleEvent extends LifecycleEvent{

	/**
	 * The schema variation associated with this event.
	 * 
	 * @return An object of type {@link Variation}.
	 * 
     * @since 0.7
	 */
	public Variation getVariation();
	
}
