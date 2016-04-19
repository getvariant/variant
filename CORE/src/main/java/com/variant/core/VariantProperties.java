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

	public static final String COMMANDLINE_RESOURCE_NAME = "variant.props.resource";
	public static final String COMMANDLINE_FILE_NAME = "varaint.props.file";
	public static final String COMMANDLINE_PROP_PREFIX = "varaint.";

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
		SESSION_ID_TRACKER_CLASS_NAME,
		SESSION_ID_TRACKER_CLASS_INIT,
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
	
	/**
	 * <p> Equivalent to <code>get(key, Class<String>)</code>.
	 * 
	 * @param key Property key
	 *              
	 * @return Raw String value.
	 */	
	public String get(Key key);
	
	/**
	 * The source of where the value came from, such as the name of the application properties resurce file.
	 *  
	 * @param key
	 * @return
	 */
	public String getSource(Key key);
}
