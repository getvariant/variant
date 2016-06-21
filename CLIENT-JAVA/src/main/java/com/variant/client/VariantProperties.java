package com.variant.client;

import com.variant.core.VariantCoreProperties;

public interface VariantProperties extends VariantCoreProperties {

	/**
	 * Keys of all known core properties.
	 */
	public final static Key SESSION_ID_TRACKER_CLASS_NAME = new Key();
	public final static Key SESSION_ID_TRACKER_CLASS_INIT = new Key();
	public final static Key TARGETING_TRACKER_CLASS_NAME = new Key();
	public final static Key TARGETING_TRACKER_CLASS_INIT = new Key();

}
