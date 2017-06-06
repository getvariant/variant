package com.variant.core.schema;

/**
 * Representation of Hook meta XDM element.
 * 
 * @author Igor Urisman
 * @since 0.7
 */

public interface Hook {
	
	/**
	 * The name of this user hook.
	 * 
	 * @return The name of this hook. Never null.
	 * @since 0.7
	 */
	public String getName();	
	
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

}
