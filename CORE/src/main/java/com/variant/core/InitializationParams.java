package com.variant.core;

import com.variant.core.exception.VariantRuntimeException;

/**
 * Initialization parameters are passed to Variant-instantiated objects
 * as an implementation of this. The content comes from a structured
 * application property provided in a .props file as a JSON string.
 *  
 * @author Igor Urisman
 * @since 0.1
 */
public interface InitializationParams {

	/**
	 * The value of an init parameter.
	 * @param param Parameter name
	 * @return Its value or null if not defined.
     *
	 * @since 1.0
	 */
	public String get(String param);

	/**
	 * The value of an init parameter.
	 * @param param Parameter name
	 * @param exceptionIfNull The exception to be thrown if param not provided.
	 * 
	 * @return Its value or throws an exception if not defined.
     *
	 * @since 1.0
	 */
	public String getOrThrow(String param, VariantRuntimeException exceptionIfNull);
}