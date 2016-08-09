package com.variant.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import com.variant.core.event.impl.util.VariantReflectUtils;
import com.variant.core.exception.VariantInternalException;


/**
 * Extensible interface that holds all Core system property keys as final statics.
 * Client and server will extend this to include application properties specific to
 * those environments. At run time, these keys will have values, as defined in the
 * external system property files.
 *
 * @author Igor Urisman
 * @since 0.6
 * @see VariantProperties
 */
public interface VariantCorePropertyKeys {

	public final static Key EVENT_PERSISTER_CLASS_NAME = new Key("event.persister.class.name", "com.variant.core.event.EventPersisterAppLogger");
	public final static Key EVENT_PERSISTER_CLASS_INIT = new Key("event.persister.class.init", "{}");
	public final static Key EVENT_WRITER_BUFFER_SIZE = new Key("event.writer.buffer.size", "20000");
	public final static Key EVENT_WRITER_MAX_DELAY_MILLIS = new Key("event.writer.max.delay.millis", "30000");
	public final static Key EVENT_WRITER_PERCENT_FULL = new Key("event.writer.percent.full", "50");
	
	/**
	 * <p>Type representing an system property key.
	 * 
	 * @since 0.6
	 */
	public static class Key {
		
		private String defaultValue = null;
		private String propName;
				
		/**
		 * All keys defined in the passed class.
		 *  
		 * @param clazz This or subclass' {@code Class}.
		 * @return A collection of all keys defined by the passed class and all of its superclasses.
		 */
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

		/**
		 * Constructor.
		 * @param propName System property name.
		 * @param defaultValue Property's default value.
    	 * @since 0.6
		 */
		public Key(String propName, String defaultValue) {
			this.propName = propName;
			this.defaultValue = defaultValue;
		}
		
		/**
		 * Property name.
		 * @return Property name.
    	 * @since 0.6
		 */
		public String propertyName() {
			return propName;
		}
		
		/**
		 * Property's default value.
		 * @return Property's default value.
    	 * @since 0.6
		 */
		public String defaultValue() {
			return defaultValue;
		}
	}

}
