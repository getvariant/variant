package com.variant.core.session;

import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionStore;

/**
 * Potentially distributed session store built on
 * Hazelcast (hazelcast.org) distributed cache.
 * Not currently used.
 * 
 * @author Igor
 *
 */
public class SessionStoreHazelcast implements VariantSessionStore {

	private HazelcastInstance instance = null;
	private static final String MAP_NAME = "localSessionStoreMap";
			
	SessionStoreHazelcast() {
		instance = Hazelcast.newHazelcastInstance();
	}

	@Override
	public void put(String key, VariantSession value) {
		Map<String, VariantSession> store = instance.getMap(MAP_NAME);
		store.put(key, value);
	}

	@Override
	public VariantSession get(String key) {
		Map<String, VariantSession> store = instance.getMap(MAP_NAME);
		return store.get(key);
	}
	
	@Override
	public void shutdown() {
		instance.shutdown();
	}
}
