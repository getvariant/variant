package com.variant.core.flashpoint;

import com.variant.core.VariantSession;

/**
 * <p>Super-interface for all flashpoint types that occur at run time.
 * 
 * @author Igor Urisman.
 * @since 0.5
 *
 */
public interface RuntimeFlashpoint extends Flashpoint {

	/**
	 * Client code may obtain to the current Variant session.
	 * 
	 * @return An object of type {@link com.variant.core.VariantSession}.
     * @since 0.5
	 */
	public VariantSession getSession() ;
	
}
