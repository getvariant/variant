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
	 * @return The name of this test.
	 * @since 0.7
	 */
	public String getName();	
	
	/**
	 * The name of the class implementing this hook.
	 * 
	 * @return The value of a state parameter, if declared by this state, null otherwise.
	 * @since 0.7
	 */
	public String getClassName();

}
