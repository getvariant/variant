package com.variant.client.impl;

/**
 */
public interface ConfigKeys {
	
	// "Public" keys set by VariantClient.Builder
	public static final String SESSION_ID_TRACKER_CLASS = "session.id.tracker.class";
	public static final String TARGETING_TRACKER_CLASS  = "targeting.tracker.class";
	public static final String TARGETING_STABILITY_DAYS = "targeting.stability.days";
	
	
	// Secret JVM properties
	public static final String SYS_PROP_TIMERS = "variant.timers";
	
}

