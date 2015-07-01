package com.variant.core.session;

import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantRuntimeException;
import com.variant.core.VariantSession;
import com.variant.core.error.ErrorTemplate;

public class SessionService {

	private SessionKeyResolver keyResolver = null;
	private SessionStore store = null;
	
	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public SessionService(Config config) throws VariantBootstrapException {
		
		// Session store directly from factory.
		store = SessionStore.Factory.getInstance(config.type);
		
		// Session key resolver 
		try {
			Class<?> persisterClass = Class.forName(config.keyResolverClassName);
			Object persisterObject = persisterClass.newInstance();
			if (persisterObject instanceof SessionKeyResolver) {
				keyResolver = (SessionKeyResolver) persisterObject;
			}
			else {
				throw new VariantBootstrapException(
						"Session Key Resolver class [" + 
				config.keyResolverClassName + 
				"] must implement interface [" +
				SessionKeyResolver.class.getName()
				);
			}
		}
		catch (Exception e) {
			throw new VariantBootstrapException(
					"Unable to instantiate Session Key Resolver class [" +
					config.keyResolverClassName +
					"]",
					e
			);
		}

	}
	
	/**
	 * Get user session.
	 * @param create indicates whether to create the session if doesn't yet exist.
	 * 
	 * @return user session if exists or null if doesn't and create is false.
	 */
	public VariantSession getSession(boolean create, SessionKeyResolver.UserData...userArgs) {
		
		String key = keyResolver.getSessionKey(userArgs);
		
		if (key == null) 
			throw new VariantRuntimeException(
					ErrorTemplate.INTERNAL, 
					"Unable to obtain session key from resolver [" + keyResolver.getClass().getSimpleName() + "]");
		
		VariantSession result = store.get(key);

		if (result == null && create) {
			result = new VariantSessionImpl(key);
			store.put(key, result);
		}
		return result;
	}
	
	/**
	 * 
	 * @author Igor
	 *
	 */
	public static class Config {

		// Defaults
		SessionStore.Type type = SessionStore.Type.LOCAL;
		String keyResolverClassName = null;

		public void setSessionStoreType(SessionStore.Type type) {
			this.type = type;
		}
		
		public void setKeyResolverClassName(String name) {
			keyResolverClassName = name;
		}
	}
}
