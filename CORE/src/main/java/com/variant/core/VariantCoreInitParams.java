package com.variant.core;


/**
 * <p>Initialization parameters, as parsed from a JSON string, provided in a {@code *.init} system property. 
 * Passed to Variant-instantiated objects as a concrete implementation of this. 
 *  
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantCoreInitParams {

	/**
	 * The value of an init parameter.
	 * @param param Parameter name.
	 * @return The value of the parameter, or null if undefined.
     *
	 * @since 0.6
	 */
	public Object get(String param);

	/**
	 * The value of an init parameter, or throw exception if undefined.
	 * @param param Parameter name
	 * @param exceptionIfNull The exception to be thrown if param is undefined.
	 * @return Value of the parameter or throws the provided exception if undefined.
     *
	 * @since 0.6
	 */
	public Object getOr(String param, RuntimeException exceptionIfNull);

	/**
	 * The value of an init parameter.
	 * @param param Parameter name.
	 * @param valueIfNull Default value.
	 * @return The value of the parameter, or the provided default if undefined.
     *
	 * @since 0.6
	 */
	public Object getOr(String param, Object valueIfNull);

}
