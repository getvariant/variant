package com.variant.server;

import com.variant.core.VariantCorePropertyKeys;
import static com.variant.core.VariantCorePropertyKeys.Key;

public interface ServerPropertyKeys extends VariantCorePropertyKeys {

	// Default session timeout = 15 min.
	public final static Key SESSION_TIMEOUT_SECS = new Key("session.timeout.secs", "900");

	// Default vacuuming interval = 10 sec.
	public final static Key SESSION_STORE_VACUUM_INTERVAL_SECS = new Key("session.store.vacuum.interval.secs", "10");

}
