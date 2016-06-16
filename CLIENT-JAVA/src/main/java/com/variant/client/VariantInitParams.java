package com.variant.client;

import com.variant.core.VariantCoreInitParams;

/**
 * <p>Initialization parameters, as parsed from a JSON string, provided in a *.init application property. 
 * Passed to Variant-instantiated objects as an implementation of this. 
 *  
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantInitParams extends VariantCoreInitParams {

	/**
	 * Get the instance of Variant client that instantiated this object.
	 * @return The object of type {@link VariantClient} which instantiated this object.
     *
	 * @since 0.6
	 */
	public VariantClient getVariantClient();

}
