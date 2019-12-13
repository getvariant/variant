package com.variant.share.schema;

/**
 * Representation of variation scoped lifecycle hook, defined by <code>/variations[]/hooks[]</code> array element.
 *
 * @since 0.7
 */
public interface VariationScopedHook extends Hook {
	
	/**
	 * The variation in whose scope this hook is defined.
	 * 
	 * @return An object of type {@link com.variant.share.schema.Variation}. Cannot be null.
	 * @since 0.7
	 */
	Variation getVariation();
	
}
