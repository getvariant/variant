package com.variant.client;

import com.variant.core.VariantCoreStateRequest;
import com.variant.core.schema.State;

/**
 * Represents a Variant state request, as returned by {@link VariantSession#targetForState(State)}.
 *
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
