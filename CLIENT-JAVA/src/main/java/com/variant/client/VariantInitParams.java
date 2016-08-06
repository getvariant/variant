package com.variant.client;

import com.variant.core.VariantCoreInitParams;

/**
 * Initialization parameters, as parsed from a JSON string, provided in a several *.init application properties. 
 * An instance of this type is passed to a number of Variant client instantiated objects. 
 *  
 * @see VariantSessionIdTracker#init(VariantInitParams, Object...)
 * @see VariantTargetingTracker#init(VariantInitParams, Object...)
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantInitParams extends VariantCoreInitParams {

	/**
	 * Get the instance of Variant client that instantiated this object.
	 * 
	 * @return The object of type {@link VariantClient} which instantiated this object.
	 * @since 0.6
	 */
	public VariantClient getVariantClient();

}
