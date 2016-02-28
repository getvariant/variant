package com.variant.core;


/**
 * <p>Variant applicaiton properties.
 * 
 * @author Igor Urisman
 * 
 * @since 0.6
 */
public interface VariantProperties {
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * <p>Keys of all known properties.
	 */
	public static enum Key {
		
		EVENT_PERSISTER_CLASS_NAME,
		EVENT_PERSISTER_CLASS_INIT,
		EVENT_WRITER_BUFFER_SIZE,
		EVENT_WRITER_MAX_DELAY_MILLIS,
		EVENT_WRITER_PERCENT_FULL,
		SESSION_STORE_CLASS_NAME,
		SESSION_STORE_CLASS_INIT,
		TARGETING_TRACKER_CLASS_NAME,
		TARGETING_TRACKER_IDLE_DAYS_TO_LIVE,
		;		
		
		/**
		 * 
		 */
		public String propName() {
			StringBuilder result = new StringBuilder(this.name().toLowerCase());
			for (int i = 0; i < result.length(); i++) {
				if (result.charAt(i) == '_') {
					result.setCharAt(i, '.');
				}
			}
			return result.toString();
		}
	}
	
	/**
	 * <p> Interpreted value of a property parameterized by expected type.
	 * 
	 * @param key Property key
	 * @param clazz Class of the expected return object.  <code>String.class</class> can be always used,
	 *              in which case the raw value of the property is returned. If <code>Integer.class</code>
	 *              is used, the raw string value will be converted to integer. If {@link InitializationParams}.class
	 *              is used, the raw string will be parsed as JSON.
	 *              
	 * @return Raw or interpreted value.
	 */
	public <T> T get(Key key, Class<T> clazz);
	
}
