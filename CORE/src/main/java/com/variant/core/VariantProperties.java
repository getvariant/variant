package com.variant.core;

import com.variant.core.VariantCorePropertyKeys.Key;


/**
 * Application properties of Variant core API.
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantProperties {
	
	/**
	 * <p> Interpreted value of a property, parameterized by expected type.
	 * 
	 * @param key Property key
	 * @param clazz Class of the expected return object.  <code>String.class</class> can be always used,
	 *              in which case the raw value of the property is returned. If <code>Integer.class</code>
	 *              is used, the raw string value will be converted to integer. If {@link VariantCoreInitParams}.class
	 *              is used, the raw string will be parsed as JSON.
	 *              
	 * @return Raw or interpreted value.
	 */
	public <T> T get(Key key, Class<T> clazz);

	/**
	 * <p> Source of this key's value, e.g. "default" or the name of the classpath resource.
	 * 
	 * @param key Property key
	 * @param clazz Class of the expected return object.  <code>String.class</class> can be always used,
	 *              in which case the raw value of the property is returned. If <code>Integer.class</code>
	 *              is used, the raw string value will be converted to integer. If {@link VariantCoreInitParams}.class
	 *              is used, the raw string will be parsed as JSON.
	 *              
	 * @return Raw or interpreted value.
	 */
	public String getSource(Key key);

}
