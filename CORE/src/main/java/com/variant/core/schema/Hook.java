package com.variant.core.schema;

/**
 * Representation of Hook XDM element.
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
	
	
	/**
	 * A hook element defined at the meta (schema) level.
	 * @author Igor
	 * @since 0.7
	 */
	public interface Schema extends Hook {}
	
	/**
	 * A hook element defined at the test level.
	 * @author Igor
	 * @since 0.7
	 */
	public interface Test extends Hook {
		
		/**
		 * The test domain of this hook.
		 * 
		 * @return An object of type {@link com.variant.core.schema.Test}
		 * @since 0.7
		 */
		public com.variant.core.schema.Test getTest();
		
	}
}
