package com.variant.core.schema;

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
	 * @return The string value of the "init" property. Null, if omitted.
	 * @since 0.8
	 */
	public String getInit();
		
}
