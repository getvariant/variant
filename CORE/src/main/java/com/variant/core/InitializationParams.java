package com.variant.core;

import com.variant.core.exception.VariantRuntimeException;

/**
 * <p>Initialization parameters, as parsed from a JSON string, provided in a *.init application property. 
 * Passed to Variant-instantiated objects as an implementation of this. 
 *  
 * @author Igor Urisman
 * @since 0.6
 */
public interface InitializationParams {

	/**
	 * The value of an init parameter.
	 * @param param Parameter name.
	 * @param defaultValue Default value.
	 * @return The value of the parameter, or default value if undefined.
     *
	 * @since 0.6
	 */
	public Object getOr(String param, Object defaultValue);

	/**
	 * The value of an init parameter.
	 * @param param Parameter name
	 * @param exceptionIfNull The exception to be thrown if param is undefined.
	 * 
	 * @return Its value of the parameter or throws the provided exception, if undefined.
     *
	 * @since 0.6
	 */
	public Object getOrThrow(String param, VariantRuntimeException exceptionIfNull);

}
