package com.variant.client;

import com.variant.client.session.SessionIdTrackerImplDefault;
import com.variant.client.session.TargetingTrackerDefault;
import com.variant.core.VariantCoreInitParams;
import com.variant.core.impl.VariantCoreInitParamsImpl;

/**
 * <p>Variant system properties. 
 * Each property is a key-value pair with keys of type {@link Property} and value which is bound at deployment
 * time from classpath property files or JVM command line. Host code may obtain the raw value as a String or
 * converted to some datatype.
 *
 * @author Igor Urisman
 * @since 0.6
 * @see VariantCorePropertyKeys
 */
public interface SystemProperties {
	
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
	 *         is returned as provided by {@link Property#defaultValue()}.
	 *         
	 * @since 0.6
	 */
	public <T> T get(Property key, Class<T> clazz);

	/**
	 * <p> Uninterpreted value of an system property, given by its key, as String.
	 * 
	 * @param key Property key	 *              
	 * @return Original string value, as provided by either a properties file or the default.
	 *         
	 * @since 0.6
	 */
	public <T> T get(Property key);

	/**
	 * <p>Raw value of an system property, given by its key.
	 * 
	 * @param key Property key
	 *              
	 * @return Raw value of the key. If no value was externally bound, the default
	 *         is returned as provided by {@link Property#defaultValue()}.
	 *         
	 * @since 0.6
	 */
	public String getSource(Property key);

	/**
	 * <p>Type representing an system property.
	 * 
	 * @since 0.6
	 */
	public enum Property {

		TEST_MAX_IDLE_DAYS_TO_TARGET("test.max.idle.days.to.target", "0"),
		SESSION_ID_TRACKER_CLASS_NAME("session.id.tracker.class.name", SessionIdTrackerImplDefault.class.getName()),
		SESSION_ID_TRACKER_CLASS_INIT("session.id.tracker.class.init", "{}"),
		TARGETING_TRACKER_CLASS_NAME("targeting.tracker.class.name", TargetingTrackerDefault.class.getName()),
		TARGETING_TRACKER_CLASS_INIT("targeting.tracker.class.init", "{}"),
		SERVER_ENDPOINT_URL("server.endpoint.url", "http://localhost:8080/variant");

		public final String defaultValue;
		public final String name;
					
/*
			 * All keys defined in the passed class.
			 *  
			 * @param clazz This or subclass' {@code Class}.
			 * @return A collection of all keys defined by the passed class and all of its superclasses.
			 *
			public static Collection<Key> keys(Class<? extends VariantCorePropertyKeys> clazz) {
				Collection<Key> result = new ArrayList<Key>();
				for (Field field: VariantReflectUtils.getStaticFields(clazz, Key.class)) {
					try {
						result.add((Key)field.get(null));
					}
					catch(IllegalAccessException e) {
						throw new VariantInternalException(e);
					}
				}
				return result;
			}
*/
		/**
		 * Constructor.
		 * @param name System property name.
		 * @param value Property's default value.
    	 * @since 0.6
		 */
		private Property(String name, String defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}			
	}
}