package com.variant.client;

import com.variant.core.VariantCoreSession;
import com.variant.core.schema.State;

/**
 * 
 * @author Igor Urisman
 * @since 0.6
 *
 */
public interface VariantSession extends VariantCoreSession {

	/**
     * <p>Target session for a state. 
     *  
	 * @return An instance of the {@link com.variant.core.VariantCoreStateRequest} object, which
	 *         may be further examined for more information about the outcome of this operation.  
	 *
	 * @since 0.5
	 */
	VariantStateRequest targetForState(State state);
	
}
