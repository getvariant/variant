package com.variant.core.schema;

/**
 * Representation of the schema <code>hook</code> property. Depending on the definition context,
 * a hook can have the scope of the entire schema, a particular state, or a particular test.
 * 
 * @since 0.7
 */

public interface Hook {
			
	/**
	 * The canonical name of the class implementing this hook.
	 * 
	 * @return The string value of the "class" property. Never null.
	 * @since 0.7
	 */
	public String getClassName();

	/**
	 * The initialization string.
	 * 
	 * @return The string value of the "init" property. Null, if omitted.
	 * @since 0.7
	 */
	public String getInit();
	
	
	/**
	 * Schema scoped life-cycle hook. Declared in the {@code meta/hooks} array.
     *
	 * @since 0.7
	 */
	public interface Schema extends Hook {}
	
	/**
	 * State scoped life-cycle hook. Declared in the {@code state//hooks} array.
	 *
	 * @since 0.7
	 */
	public interface State extends Hook {
		
		/**
		 * The state in whose scope this hook is defined.
		 * 
		 * @return An object of type {@link com.variant.core.schema.Variation}
		 * @since 0.7
		 */
		public com.variant.core.schema.State getState();
		
	}

	/**
	 * Variation-scoped life-cycle hook. Declared in the {@code variations//hooks} array
	 * @author Igor
	 * @since 0.7
	 */
	public interface Variation extends Hook {
		
		/**
		 * The variation in whose scope this hook is defined.
		 * 
		 * @return An object of type {@link com.variant.core.schema.Variation}
		 * @since 0.7
		 */
		public com.variant.core.schema.Variation getVariation();
		
	}
}
