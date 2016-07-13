package com.variant.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;

import com.variant.core.exception.VariantInternalException;
import com.variant.core.util.ReflectUtils;


/**
 * Keys of all Core properties.
 *
 * @author Igor Urisman
 *
 */
public interface VariantCorePropertyKeys {

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
		
		private String defaultValue = null;
		private String propName;
				
		/**
		 * All keys defined in the passed class.
		 * @param clazz
		 * @return
		 */
		public static Collection<Key> keys(Class<? extends VariantCorePropertyKeys> clazz) {
			Collection<Key> result = new ArrayList<Key>();
			for (Field field: ReflectUtils.getStaticFields(clazz, Key.class)) {
				try {
					result.add((Key)field.get(null));
				}
				catch(IllegalAccessException e) {
					throw new VariantInternalException(e);
				}
			}
			return result;
		}

		public Key(String propName, String defaultValue) {
			this.propName = propName;
			this.defaultValue = defaultValue;
		}
		
		public String propertyName() {
			return propName;
		}
		
		public String defaultValue() {
			return defaultValue;
		}
	}

}
