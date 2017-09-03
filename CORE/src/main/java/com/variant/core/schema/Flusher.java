package com.variant.core.schema;

/**
 * Representation of the meta/flusher schema property.
 * 
 * @author Igor Urisman
 * @since 0.8
 */

public interface Flusher {
	
	/**
	 * The canonical name of the class implementing this flusher.
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
