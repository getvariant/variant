package com.variant.core;

import com.variant.core.VariantCorePropertyKeys.Key;
import com.variant.core.impl.VariantCoreInitParamsImpl;


/**
 * <p>Variant system properties. 
 * Each property is a key-value pair with keys of type {@link Key} and value which is bound at deployment
 * time from classpath property files or JVM command line. Host code may obtain the raw value as a String or
 * converted to some datatype.
 *
 * @author Igor Urisman
 * @since 0.6
 * @see VariantCorePropertyKeys
 */
public interface VariantProperties {
	
	/**
	 * <p> Interpreted value of an system property, given by its key, parameterized by expected type.
	 * 
	 * @param key Property key
	 * @param clazz Class of the expected return object. {@code String.class} can be always used,
	 *              in which case the raw value of the property is returned. If {@code Integer.class} or {@code Long.class}
	 *              is used, the raw string value will be converted to Integer or Long respectively. If 
	 *              {@link VariantCoreInitParams}.class is used, the raw string will be parsed as JSON and converted
	 *              to {@link VariantCoreInitParamsImpl}.
	 *              
	 * @return Interpreted value of the key as an instance of the given class. If no value was externally bound, the default
	 *         is returned as provided by {@link Key#defaultValue()}.
	 *         
	 * @since 0.6
	 */
	public <T> T get(Key key, Class<T> clazz);

	/**
	 * <p>Raw value of an system property, given by its key.
	 * 
	 * @param key Property key
	 *              
	 * @return Raw value of the key. If no value was externally bound, the default
	 *         is returned as provided by {@link Key#defaultValue()}.
	 *         
	 * @since 0.6
	 */
	public String getSource(Key key);

}
