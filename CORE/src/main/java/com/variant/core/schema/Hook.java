package com.variant.core.schema;

import java.util.Optional;

/**
 * Representation of various lifecycle hook schema elements. Depending on where in the variation schema it is defined,
 * a hook can have the scope of the entire schema, of a particular state, or of a particular variation.
 * 
 * @since 0.7
 */

public interface Hook {
			
	/**
	 * The canonical name of the class implementing this lifecycle hook, as provided by the <code>/../hooks[]/class</code> schema element.
	 * Variant instantiates an instance of this class for each subscribed lifecycle event.
	 * 
	 * @return The string value provided by the <code>/../hooks[]/class</code> schema element. Cannot be null.
	 * @since 0.7
	 */
	public String getClassName();

	/**
	 * Arbitrary object used to initialize a newly instantiated lifecycle hook, as provided by the 
	 * <code>/../hooks[]/init</code> schema element. Variant will pass this object to the constructor 
	 * of the hook class.
	 * 
	 * @return The Optional container, with the serialized string value of the <code>/../hooks[]/init</code> element,
	 *         if defined, or empty if <code>/../hooks[]/init</code> element was omitted.
	 * @since 0.7
	 */
	public Optional<String> getInit();
	
	
	/**
	 * Schema scoped lifecycle hook, defined by <code>/meta/hooks[]</code> array element.
     *
	 * @since 0.7
	 */
	public interface Schema extends Hook {}
	
	/**
	 * State scoped lifecycle hook, defined by <code>/states[]/hooks[]</code> array element.
	 *
	 * @since 0.7
	 */
	public interface State extends Hook {
		
		/**
		 * The state in whose scope this hook is defined.
		 * 
		 * @return An object of type {@link com.variant.core.schema.State}
		 * @since 0.7
		 */
		public com.variant.core.schema.State getState();
		
	}

	/**
	 * Variation scoped lifecycle hook, defined by <code>/variations[]/hooks[]</code> array element.
	 *
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
