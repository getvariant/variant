package com.variant.core;


/**
 * Application properties of Variant core API.
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantCoreProperties {

	public static final String COMMANDLINE_RESOURCE_NAME = "variant.props.resource";
	public static final String COMMANDLINE_FILE_NAME = "varaint.props.file";
	public static final String COMMANDLINE_PROP_PREFIX = "varaint.";

	/**
	 * Keys of all known core properties.
	 */
	public final static Key TEST_MAX_IDLE_DAYS_TO_TARGET = new Key();
	public final static Key EVENT_PERSISTER_CLASS_NAME = new Key();
	public final static Key EVENT_PERSISTER_CLASS_INIT = new Key();
	public final static Key EVENT_WRITER_BUFFER_SIZE = new Key();
	public final static Key EVENT_WRITER_MAX_DELAY_MILLIS = new Key();
	public final static Key EVENT_WRITER_PERCENT_FULL = new Key();
	public final static Key SESSION_STORE_CLASS_NAME = new Key();
	public final static Key SESSION_STORE_CLASS_INIT = new Key();
	
	public final Key SERVER_ENDPOINT_URL = new Key();

	/**
	 * <p>Property key. The corresponding name is derived by lower-casing
	 * and replacing all underscores ('_') with periods ('.').
	 */
	public static class Key {
		public Key() {}
		public String propertyName() {
			StringBuilder result = new StringBuilder(getClass().getSimpleName().toLowerCase());
			for (int i = 0; i < result.length(); i++) {
				if (result.charAt(i) == '_') {
					result.setCharAt(i, '.');
				}
			}
			return result.toString();
		}
	}
	
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
	
}
