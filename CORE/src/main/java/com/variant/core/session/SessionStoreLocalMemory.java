package com.variant.core.session;

import java.util.HashMap;

import com.variant.core.VariantSession;
import com.variant.core.VariantSessionStore;

/**
 * Simplest session store implementation in local memory.
 * Good for tests only.
 * 
 * @author Igor
 *
 */
public class SessionStoreLocalMemory implements VariantSessionStore {

	private HashMap<String, VariantSession> map = new HashMap<String, VariantSession>();
			
	SessionStoreLocalMemory() { }
	
	@Override
	public void put(String key, VariantSession value) {
		map.put(key, value);
	}

	@Override
	public VariantSession get(String key) {
		return map.get(key);
	}
	
	@Override
	public void shutdown() {
		map = null;
	}
}
