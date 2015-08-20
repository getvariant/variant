package com.variant.core.session;

import java.util.Map;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.variant.core.VariantSession;

/**
 * 
 * @author Igor
 *
 */
public interface SessionStore {
		
	public enum Type {LOCAL, REMOTE};
	
	public VariantSession get(String key);
	public void put(String key, VariantSession value);	
	public void shutdown(); 	// Inly local needs this.
	
	/**
	 * Factory class
	 * @author Igor
	 *
	 */
	public static class Factory {
		
		public static SessionStore getInstance(Type type) {
			
			switch (type) {
			case LOCAL:
				return new Local();
				
			default:
				throw new UnsupportedOperationException(
						String.format("Don't know what to do for type [%s]", type));
			}
		}
		
		/**
		 * Local implementation.
		 * Starts a single-node Hazelcast server on the local host.
		 * 
		 * @author Igor.
		 *
		 */
		private static class Local implements SessionStore {

			private HazelcastInstance instance = null;
			private static final String MAP_NAME = "localSessionStoreMap";
					
			private Local() {
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
		
		/**
		 * Remote implementation.  TODO
		 * @author Igor.
		 *
		 *
		private static class Remote implements SessionStore {

			private HazelcastInstance instance = null;
			private static final String MAP_NAME = "remoteSessionStoreMap";
					
			private Remote() {
				instance = Hazelcast.newHazelcastInstance();
			}

			@Override
			public void put(String key, VariantSession value) {
				throw new UnsupportedOperationException("Not yet implemented");
			}

			@Override
			public VariantSession get(String key) {
				throw new UnsupportedOperationException("Not yet implemented");
			}
			
			@Override
			public void shutdown() {
				throw new UnsupportedOperationException("Cannot shutdown remote Hazelcast server");
			}
		}
        */
	}
	
}
