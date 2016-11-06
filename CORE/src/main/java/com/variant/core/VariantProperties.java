package com.variant.core;



/**
 * <p>Variant system properties. 
 * Each property is a key-value pair with keys of type {@link Key} and value which is bound at deployment
 * time from classpath property files or JVM command line. Client and server have their own implementations.
 *
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantProperties {
	
	/**
	 * <p> System property as a String, given by its key.
	 * 
	 * @param key Property key              
	 * @return The raw value, as externally bound, or the default, converted to String.
	 *         
	 * @since 0.6
	 */
	public String getString(Key key);

	/**
	 * <p> System property as a long, given by its key.
	 * 
	 * @param key Property key
	 * @return The raw value, as externally bound, or the default, converted to long.
	 *         
	 * @since 0.6
	 */
	public long getLong(Key key);

	/**
	 * <p>Type representing a system property key. 
	 * Client and server have their own implementations.
	 * 
	 * @author Igor Urisman
	 * @since 0.6
	 */
	
	public static interface Key {

		/**
		 * <p>The name of the implementation dependent external system property bound to this key.
		 * 
    	 * @return Property Name
		 * @since 0.7
		 */
		String getExternalName();

		/**
		 * <p>The default value for this key.
		 * 
    	 * @return The default value.
		 * @since 0.7
		 */
		Object getDefault();
	}
}
