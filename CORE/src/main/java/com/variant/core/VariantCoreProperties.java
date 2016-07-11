package com.variant.core;

import java.util.LinkedHashSet;
import java.util.Set;


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
	public final static Key EVENT_PERSISTER_CLASS_NAME = new Key("event.persister.class.name", "com.variant.core.event.EventPersisterAppLogger");
	public final static Key EVENT_PERSISTER_CLASS_INIT = new Key("event.persister.class.init", "{}");
	public final static Key EVENT_WRITER_BUFFER_SIZE = new Key("event.writer.buffer.size", "20000");
	public final static Key EVENT_WRITER_MAX_DELAY_MILLIS = new Key("event.writer.max.delay.millis", "30000");
	public final static Key EVENT_WRITER_PERCENT_FULL = new Key("event.writer.percent.full", "90");
	
	/**
	 * <p>Property key. The corresponding name is derived by lower-casing
	 * and replacing all underscores ('_') with periods ('.').
	 */
	public static class Key {
		
		String defaultValue = null;

		private static LinkedHashSet<Key> allKeys = new LinkedHashSet<Key>(); 
		private String propName;
		
		public static Set<Key> keySet() {
			return allKeys;
		}
		
		public Key(String propName, String defaultValue) {
			this.propName = propName;
			this.defaultValue = defaultValue;
			allKeys.add(this);
		}
		
		public String propertyName() {
			return propName;
		}
		
		public String defaultValue() {
			return defaultValue;
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
