package com.variant.core.session;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;

import com.variant.core.Variant;
import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantInternalException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantRuntimeException;
import com.variant.core.VariantSession;

import static com.variant.core.error.ErrorTemplate.*;

import com.variant.core.impl.VariantCoreImpl;

public class SessionService {

	private Logger logger = ((VariantCoreImpl) Variant.Factory.getInstance()).getLogger();
	private SessionIdPersister sidPersister = null;
	private SessionStore store = null;
	
	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public SessionService() throws VariantBootstrapException {
		
		// Session store directly from factory.
		store = SessionStore.Factory.getInstance(VariantProperties.getInstance().sessionStoreType());
		
		// Session key resolver 
		String className = VariantProperties.getInstance().sessionKeyResolverClassName();
		try {
			Class<?> persisterClass = Class.forName(className);
			Object persisterObject = persisterClass.newInstance();
			if (persisterObject instanceof SessionIdPersister) {
				sidPersister = (SessionIdPersister) persisterObject;
			}
			else {
				throw new VariantBootstrapException(BOOT_SID_PERSISTER_NO_INTERFACE, className, SessionIdPersister.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate Session Key Resolver class [" + className + "]", e);
		}

	}
	
	/**
	 * Shutdown this SessionService.
	 * Cannot be used after this call.
	 */
	public void shutdown() {
		long now = System.currentTimeMillis();
		store.shutdown();
		store = null;
		sidPersister = null;
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Session Service shutdown in " + (DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
		}
	}
	
	/**
	 * Get user session.
	 * @param create indicates whether to create the session if doesn't yet exist.
	 * 
	 * @return user session if exists or null if doesn't and create is false.
	 */
	public VariantSession getSession(boolean create, Object userData) throws VariantRuntimeException {
		
		String key = sidPersister.get(userData);
		
		if (key == null) 
			throw new VariantRuntimeException(INTERNAL, 
					"Unable to obtain session ID from persister [" + sidPersister.getClass().getSimpleName() + "]");
		
		VariantSession result = store.get(key);

		if (result == null && create) {
			result = new VariantSessionImpl(key);
			store.put(key, result);
		}
		return result;
	}
	
	/**
	 * Persist user session Id.
	 * @param session
	 * @param userData
	 */
	public void persistSessionId(VariantSession session, Object userData) {
		sidPersister.persist(session.getId(), userData);
	}
}
