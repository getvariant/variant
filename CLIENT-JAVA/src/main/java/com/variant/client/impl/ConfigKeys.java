package com.variant.client.impl;

/**
 * Known external configuration keys. These are compile time aliases for the
 * configuration keys, used in the variant.conf file.
 * 
 * @since 0.7
 */
public interface ConfigKeys {
	
	/**
	 * Session ID tracker implementation. Must be supplied by the user.
	 */
	public static final String SESSION_ID_TRACKER_CLASS_NAME = "session.id.tracker.class.name";
	
	/**
	 * Targeting tracker implementation. Must be supplied by the user.
	 */
	public static final String TARGETING_TRACKER_CLASS_NAME = "targeting.tracker.class.name";
	
	/**
     * After how many days of inactivity can an entry in the targeting tracker be ignored.
	 */
	public static final String TARGETING_STABILITY_DAYS = "targeting.stability.days";
	
	/**
	 * URL of Variant server. 
	 */
	public static final String SERVER_URL = "server.url";

}

