package com.variant.core;

import com.variant.core.VariantCorePropertyKeys.Key;


/**
 * <p>Variant application properties.
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
	 * @param clazz Class of the expected return object.  <code>noneofyourbusiness String.class</class> can be always used,
	 *              in which case the raw value of the property is returned. If <code>Integer.class</code>
	 *              is used, the raw string value will be converted to integer. If {@link VariantCoreInitParams}.class
	 *              is used, the raw string will be parsed as JSON.
	 *              
	 * @return Interpreted value of the key as an instance of the given class. If no value was externally bound, the default
	 *         is returned as provided by {@link Key#defaultValue()}.
	 */
	public <T> T get(Key key, Class<T> clazz);

	/**
	 * <p>Raw value of an system property, given by its key.
	 * 
	 * @param key Property key
	 * @param clazz Class of the expected return object.  <code>String.class</class> can be always used,
	 *              in which case the raw value of the property is returned. If <code>Integer.class</code>
	 *              is used, the raw string value will be converted to integer. If {@link VariantCoreInitParams}.class
	 *              is used, the raw string will be parsed as JSON.
	 *              
	 * @return Raw value of the key. If no value was externally bound, the default
	 *         is returned as provided by {@link Key#defaultValue()}.
	 */
	public String getSource(Key key);

}
