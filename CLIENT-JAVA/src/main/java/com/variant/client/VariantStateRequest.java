package com.variant.client;

import com.variant.core.VariantCoreStateRequest;

/**
 * Client-side state request. A specialization of the core state request.
 * Adds the environment-dependent {@link #commit(Object...)}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 *
 */
public interface VariantStateRequest extends VariantCoreStateRequest {

	/**
	 * Environment-depended signature replaces the inherited {@link #commit()}.
	 * 
	 * @param userData   An array of zero or more opaque objects which will be passed to {@link VariantSessionIdTracker#save(Object...)}
	 * and {@link VariantTargetingTracker#save(Object...)} methods without interpretation.
	 */
	void commit(Object...userData);
}
