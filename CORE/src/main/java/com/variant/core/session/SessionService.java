package com.variant.core.session;

import static com.variant.core.schema.parser.MessageTemplate.BOOT_SID_PERSISTER_NO_INTERFACE;
import static com.variant.core.schema.parser.MessageTemplate.INTERNAL;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;

public class SessionService {

	private static final Logger LOG  = LoggerFactory.getLogger(SessionService.class);
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
		
		// Session ID persister 
		String className = VariantProperties.getInstance().sessionIdPersisterClassName();
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
		if (LOG.isDebugEnabled()) {
			LOG.debug(
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
