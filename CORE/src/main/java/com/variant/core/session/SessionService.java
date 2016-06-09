package com.variant.core.session;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.impl.MessageTemplate;

public class SessionService {

	private static final Logger LOG  = LoggerFactory.getLogger(SessionService.class);
	private VariantCore coreApi = null;
	private VariantSessionIdTracker sidTracker = null;
	private VariantSessionStore sessionStore = null;
	
	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public SessionService(VariantCore coreApi) throws VariantBootstrapException {
		
		this.coreApi = coreApi;
		
		// Session ID tracker.
		String sidTrackerClassName = coreApi.getProperties().get(VariantProperties.Key.SESSION_ID_TRACKER_CLASS_NAME, String.class);
		try {
			Class<?> sidTrackerClass = Class.forName(sidTrackerClassName);
			Object sidTrackerObject = sidTrackerClass.newInstance();
			if (sidTrackerObject instanceof VariantSessionIdTracker) {
				sidTracker = (VariantSessionIdTracker) sidTrackerObject;
				sidTracker.initialized(coreApi.getProperties().get(VariantProperties.Key.SESSION_STORE_CLASS_INIT, InitializationParams.class));
			}
			else {
				throw new VariantBootstrapException(MessageTemplate.BOOT_SESSION_ID_TRACKER_NO_INTERFACE, sidTrackerClassName, VariantSessionStore.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate session id tracker class [" + sidTrackerClassName + "]", e);
		}

		// Session store.
		String storeClassName = coreApi.getProperties().get(VariantProperties.Key.SESSION_STORE_CLASS_NAME, String.class);
		try {
			Class<?> storeClass = Class.forName(storeClassName);
			Object storeObject = storeClass.newInstance();
			if (storeObject instanceof VariantSessionStore) {
				sessionStore = (VariantSessionStore) storeObject;
				sessionStore.initialized(coreApi.getProperties().get(VariantProperties.Key.SESSION_STORE_CLASS_INIT, InitializationParams.class));
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
		sidTracker.shutdown();
		sidTracker = null;
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
		
		if (coreApi.getSchema() == null) throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_UNDEFINED);

		String sessionId = sidTracker.get(userData);
		// Should never return null!
		if (sessionId == null) 
			throw new VariantRuntimeException(MessageTemplate.RUN_SESSION_ID_NULL, sidTracker.getClass().getSimpleName());

		VariantSession result = sessionStore.get(sessionId, userData);

		if (result == null) {
			result = new VariantSessionImpl(coreApi, sessionId);
			sessionStore.save(result, userData);
		}
		return result;	
	}
	
	/**
	 * Persist user session Id.
	 * @param session
	 * TODO Make this async
	 */
	public void saveSession(VariantSession session, Object...userData) {
		if (coreApi.getSchema() == null) throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_UNDEFINED);
		if (!coreApi.getSchema().getId().equals(session.getSchemaId())) 
			throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_REPLACED, coreApi.getSchema().getId(), session.getSchemaId());
		sessionStore.save(session, userData);
	}
}
