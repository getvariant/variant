package com.variant.share.schema;

/**
 * Representation of state scoped lifecycle hook, defined by <code>/states[]/hooks[]</code> array element.
 *
 * @since 0.7
 */
public interface StateScopedHook extends Hook {
	
	/**
	 * The state in whose scope this hook is defined.
	 * 
	 * @return An object of type {@link com.variant.share.schema.State}. Cannot be null.
	 * @since 0.7
	 */
	State getState();
	
}

