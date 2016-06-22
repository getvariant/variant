package com.variant.client;

import com.variant.core.VariantCoreProperties;

public interface VariantProperties extends VariantCoreProperties {

	/**
	 * Keys of all known core properties.
	 */
	public final static Key SESSION_ID_TRACKER_CLASS_NAME = new Key("session.id.tracker.class.name");
	public final static Key SESSION_ID_TRACKER_CLASS_INIT = new Key("session.id.tracker.class.init");
	public final static Key TARGETING_TRACKER_CLASS_NAME = new Key("targeting.tracker.class.name");
	public final static Key TARGETING_TRACKER_CLASS_INIT = new Key("targeting.tracker.class.init");
	public final static Key SERVER_ENDPOINT_URL = new Key("server.endpoint.url");

}
