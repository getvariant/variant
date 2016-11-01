package com.variant.client;

import com.variant.client.session.SessionIdTrackerImplDefault;
import com.variant.client.session.TargetingTrackerDefault;
/**
 * Variant Java client application properties.
 * 
 * @Author: Igor Urisman.
 * @since 0.6
 */

/**
 * Client and server will extend this to include application properties specific to
 * those environments. At run time, these keys will have values, as defined in the
 * external system property files.
 *
 * @author Igor Urisman
 * @since 0.6
 * @see SystemProperties
 *
public interface VariantClientPropertyKeys {

	public final static Key TEST_MAX_IDLE_DAYS_TO_TARGET  = new Key("test.max.idle.days.to.target", "0");
	public final static Key SESSION_ID_TRACKER_CLASS_NAME = new Key("session.id.tracker.class.name", SessionIdTrackerImplDefault.class.getName());
	public final static Key SESSION_ID_TRACKER_CLASS_INIT = new Key("session.id.tracker.class.init", "{}");
	public final static Key TARGETING_TRACKER_CLASS_NAME  = new Key("targeting.tracker.class.name", TargetingTrackerDefault.class.getName());
	public final static Key TARGETING_TRACKER_CLASS_INIT  = new Key("targeting.tracker.class.init", "{}");
	public final static Key SERVER_ENDPOINT_URL           = new Key("server.endpoint.url", "http://localhost:8080/variant");
	
	/**
	 * <p>Type representing an system property key.
	 * 
	 * @since 0.6
	 *
	public static class Key {
		
		private String defaultValue = null;
		private String propName;
				
		/**
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
		 * @param propName System property name.
		 * @param defaultValue Property's default value.
    	 * @since 0.6
		 *
		private Key(String propName, String defaultValue) {
			this.propName = propName;
			this.defaultValue = defaultValue;
		}
		
		/**
		 * Property name.
		 * @return Property name.
    	 * @since 0.6
		 *
		public String propertyName() {
			return propName;
		}
		
		/**
		 * Property's default value.
		 * @return Property's default value.
    	 * @since 0.6
		 *
		public String defaultValue() {
			return defaultValue;
		}
	}

}
*/