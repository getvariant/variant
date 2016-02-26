package com.variant.core.session;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;
import com.variant.core.config.VariantProperties;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.impl.MessageTemplate;

import static com.variant.core.schema.impl.MessageTemplate.*;

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
		String storeClassName = VariantProperties.getInstance().sessionStoreClassName();
		try {
			Class<?> storeClass = Class.forName(storeClassName);
			Object storeObject = storeClass.newInstance();
			if (storeObject instanceof VariantSessionStore) {
				sessionStore = (VariantSessionStore) storeObject;
				sessionStore.initialized(VariantProperties.getInstance().sessionStoreClassInit());
			}
			else {
				throw new VariantBootstrapException(MessageTemplate.BOOT_SESSION_STORE_NO_INTERFACE, storeClassName, VariantSessionStore.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate session store class [" + storeClassName + "]", e);
		}

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
	 * @param userData opaque object(s) as passed to Variant.getSession()
	 * @return 
	 */
	public VariantSession getSession(Object...userData) throws VariantRuntimeException {
				
		VariantSession result = sessionStore.get(userData);

		if (result == null) {

			VariantSessionIdTracker sidTracker = sessionStore.getSessionIdTracker();
			String sessionId = sidTracker.get(userData);
			
			if (sessionId == null) 
				throw new VariantRuntimeException(INTERNAL, 
						"Unable to obtain session ID from tracker [" + sidTracker.getClass().getSimpleName() + "]");

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
