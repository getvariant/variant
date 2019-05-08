package com.variant.core.schema;

import java.util.Optional;

/**
 * Representation of the event flusher schema property.
 * 
 * @since 0.8
 */

public interface Flusher {
	
	/**
	 * The name of the class implementing this flusher. Variant will attempt to load this class dynamically by this name,
	 * as in {@link Class#forName(String)}.
	 * 
	 * @return The string value of the "class" property. Never null.
	 * @since 0.8
	 */
	public String getClassName();

	/**
	 * The initialization string.
	 * 
	 * @return The <code>Optional</code> container, with the string value of the "init" property,
	 *         if provided in the schema, or empty if no <code>'init'</code> property was set.
	 * @since 0.8
	 */
	public Optional<String> getInit();
		
}
