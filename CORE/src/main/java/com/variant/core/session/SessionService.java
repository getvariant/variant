package com.variant.core.session;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantBootstrapException;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantRuntimeException;

import static com.variant.core.schema.parser.MessageTemplate.*;

public class SessionService {

	private static final Logger LOG  = LoggerFactory.getLogger(SessionService.class);
	private VariantSessionStore sessionStore = null;
	
	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public SessionService() throws VariantBootstrapException {
		
		// Session store.
		sessionStore = SessionStoreFactory.getInstance(VariantProperties.getInstance().sessionStoreClassName());
		
	}
	
	/**
	 * Shutdown this SessionService.
	 * Cannot be used after this call.
	 */
	public void shutdown() {
		long now = System.currentTimeMillis();
		sessionStore.shutdown();
		sessionStore = null;
		if (LOG.isDebugEnabled()) {
			LOG.debug(
					"Session Service shutdown in " + (DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
		}
	}
	
	/**
	 * Get or create user session.
	 * @return 
	 */
	public VariantSession getSession(Object...userData) throws VariantRuntimeException {
				
		VariantSession result = sessionStore.get(userData);

		if (result == null) {

			VariantSessionIdTracker sidTracker = sessionStore.getSessionIdTracker();
			String sessionId = sidTracker.get(userData);
			
			if (sessionId == null) 
				throw new VariantRuntimeException(INTERNAL, 
						"Unable to obtain session ID from persister [" + sidTracker.getClass().getSimpleName() + "]");

			result = new VariantSessionImpl(sessionId);
			sessionStore.save(result, userData);
		}
		return result;	}
	
	/**
	 * Persist user session Id.
	 * @param session
	 */
	public void saveSession(VariantSession session, Object...userData) {
		sessionStore.save(session, userData);
	}
}
