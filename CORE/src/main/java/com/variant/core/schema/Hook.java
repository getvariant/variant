package com.variant.core.schema;

/**
 * Representation of Hook meta XDM element.
 * 
 * @author Igor Urisman
 * @since 0.7
 */

public interface Hook {
	
	public enum Domain {
		SCHEMA, TEST
	}
	
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
	
	/**
	 * The domain of this hook.
	 * 
	 * @return An element of the {@link Domain} enumeration.
	 * 
	 * @since 0.7
	 */
	public Domain getDomain();

}
