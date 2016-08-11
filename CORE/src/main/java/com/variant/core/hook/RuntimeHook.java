package com.variant.core.hook;

import com.variant.core.VariantCoreSession;

/**
 * <p>Super-interface for all user hook types that post their listeners 
 * at run time.
 * 
 * @author Igor.
 * @since 0.5
 *
 */
public interface RuntimeHook extends UserHook {

	/**
	 * Host code can obtain the current Variant session.
	 * 
	 * @return An object of type {@link com.variant.core.VariantCoreSession}.
     * @since 0.5
	 */
	public VariantCoreSession getSession() ;
	
}
