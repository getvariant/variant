package com.variant.core.session;

import static com.variant.core.schema.parser.MessageTemplate.INTERNAL;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantRuntimeException;

public class SessionService {

	private static final Logger LOG  = LoggerFactory.getLogger(SessionService.class);
	private VariantSessionIdTracker sidTracker = null;
	private VariantSessionStore sessionStore = null;
	
	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public SessionService() throws VariantBootstrapException {
		
		// Session store.
		sessionStore = SessionStoreFactory.getInstance(VariantProperties.getInstance().sessionStoreClassName());
		
		// Session ID persister 
		sidTracker = SessionIdTrackerFactory.getInstance(VariantProperties.getInstance().sessionIdPersisterClassName());

	}
	
	/**
	 * Shutdown this SessionService.
	 * Cannot be used after this call.
	 */
	public void shutdown() {
		long now = System.currentTimeMillis();
		sessionStore.shutdown();
		sessionStore = null;
		sidTracker = null;
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
		
		String key = sidTracker.get(userData);
		
		if (key == null) 
			throw new VariantRuntimeException(INTERNAL, 
					"Unable to obtain session ID from persister [" + sidTracker.getClass().getSimpleName() + "]");
		
		VariantSession result = sessionStore.get(key);

		if (result == null && create) {
			result = new VariantSessionImpl(key);
			sessionStore.put(key, result);
		}
		return result;
	}
	
	/**
	 * Persist user session Id.
	 * @param session
	 * @param userData
	 */
	public void persistSessionId(VariantSession session, Object userData) {
		sidTracker.persist(session.getId(), userData);
	}
}
