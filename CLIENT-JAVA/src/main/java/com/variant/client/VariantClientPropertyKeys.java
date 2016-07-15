package com.variant.client;

import com.variant.client.session.SessionIdTrackerImplDefault;
import com.variant.client.session.TargetingTrackerDefault;
import com.variant.core.VariantCorePropertyKeys;

/**
 * Client-side Variant application properties.
 */
public interface VariantClientPropertyKeys extends VariantCorePropertyKeys {

	public final static Key TEST_MAX_IDLE_DAYS_TO_TARGET  = new Key("test.max.idle.days.to.target", "0");
	public final static Key SESSION_ID_TRACKER_CLASS_NAME = new Key("session.id.tracker.class.name", SessionIdTrackerImplDefault.class.getName());
	public final static Key SESSION_ID_TRACKER_CLASS_INIT = new Key("session.id.tracker.class.init", "{}");
	public final static Key TARGETING_TRACKER_CLASS_NAME  = new Key("targeting.tracker.class.name", TargetingTrackerDefault.class.getName());
	public final static Key TARGETING_TRACKER_CLASS_INIT  = new Key("targeting.tracker.class.init", "{}");
	public final static Key SERVER_ENDPOINT_URL           = new Key("server.endpoint.url", "http://localhost:8080/");

}
