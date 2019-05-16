package com.variant.core.schema;

import java.util.Optional;

/**
 * Representation of various lifecycle <code>HOOK</code> schema elements. Depending on where in the variation schema it is defined,
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
	String getClassName();

	/**
	 * Arbitrary object used to initialize a newly instantiated lifecycle hook, as provided by the 
	 * <code>/../hooks[]/init</code> schema element. Variant will pass this object to the constructor 
	 * of the hook class.
	 * 
	 * @return The Optional container, with the serialized string value of the <code>/../hooks[]/init</code> element,
	 *         if defined, or empty if <code>/../hooks[]/init</code> element was omitted.
	 * @since 0.7
	 */
	Optional<String> getInit();
	
}
