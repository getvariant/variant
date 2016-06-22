package com.variant.client.session;

import static com.variant.client.VariantProperties.SESSION_ID_TRACKER_CLASS_INIT;
import static com.variant.client.VariantProperties.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.VariantProperties.TARGETING_TRACKER_CLASS_INIT;
import static com.variant.client.VariantProperties.TARGETING_TRACKER_CLASS_NAME;
import static com.variant.core.VariantCoreProperties.SESSION_STORE_CLASS_INIT;
import static com.variant.core.VariantCoreProperties.SESSION_STORE_CLASS_NAME;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_TARGETING_TRACKER_NO_INTERFACE;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.impl.VariantClientImpl;
import com.variant.client.impl.VariantInitParamsImpl;
import com.variant.client.impl.VariantSessionImpl;
import com.variant.core.VariantCoreInitParams;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.session.SessionStore;

public class SessionService {

	private static final Logger LOG  = LoggerFactory.getLogger(SessionService.class);
	private VariantClientImpl client = null;
	private SessionStore sessionStore = null;
	
	/**
	 * Instantiate session ID tracker.
	 * TODO: reflective object creation is expensive.
	 * @param userData
	 * @return
	 */
	private VariantSessionIdTracker initSessionIdTracker(Object...userData) {
		// Session ID tracker.
		String sidTrackerClassName = client.getProperties().get(SESSION_ID_TRACKER_CLASS_NAME, String.class);
		try {
			Class<?> sidTrackerClass = Class.forName(sidTrackerClassName);
			Object sidTrackerObject = sidTrackerClass.newInstance();
			if (sidTrackerObject instanceof VariantSessionIdTracker) {
				VariantSessionIdTracker result = (VariantSessionIdTracker) sidTrackerObject;
				VariantInitParamsImpl initParams = new VariantInitParamsImpl(client, SESSION_ID_TRACKER_CLASS_INIT);
				result.initialized(initParams, userData);
				return result;
			}
			else {
				throw new VariantBootstrapException(MessageTemplate.BOOT_SESSION_ID_TRACKER_NO_INTERFACE, sidTrackerClassName, SessionStore.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate session id tracker class [" + sidTrackerClassName + "]", e);
		}

	}
		
	/**
	 * Instantiate targeting tracker.
	 * TODO: reflective object creation is expensive.
	 * @param userData
	 * @return
	 */
	private VariantTargetingTracker initTargetingTracker(Object...userData) {
		
		// Instantiate targeting tracker.
		String className = client.getCoreApi().getProperties().get(TARGETING_TRACKER_CLASS_NAME, String.class);
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof VariantTargetingTracker) {
				VariantTargetingTracker result = (VariantTargetingTracker) object;
				VariantInitParamsImpl initParams = new VariantInitParamsImpl(client, TARGETING_TRACKER_CLASS_INIT);
				result.initialized(initParams, userData);
				return result;
			}
			else {
				throw new VariantBootstrapException(BOOT_TARGETING_TRACKER_NO_INTERFACE, className, VariantTargetingTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to instantiate targeting tracker class [" + className +"]", e);
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param config
	 * @throws VariantBootstrapException 
	 */
	public SessionService(VariantClient client) throws VariantBootstrapException {
		
		this.client = (VariantClientImpl) client;
		
		// Session store.
		String storeClassName = client.getProperties().get(SESSION_STORE_CLASS_NAME, String.class);
		try {
			Class<?> storeClass = Class.forName(storeClassName);
			Object storeObject = storeClass.newInstance();
			if (storeObject instanceof SessionStore) {
				sessionStore = (SessionStore) storeObject;
				sessionStore.initialized(client.getProperties().get(SESSION_STORE_CLASS_INIT, VariantCoreInitParams.class));
			}
			else {
				throw new VariantBootstrapException(MessageTemplate.BOOT_SESSION_STORE_NO_INTERFACE, storeClassName, SessionStore.class.getName());
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
		
		if (client.getSchema() == null) throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_UNDEFINED);

		// Get session ID from the session ID tracker.
		VariantSessionIdTracker sidTracker = initSessionIdTracker(userData);
		String sessionId = sidTracker.get();
		// Should never return null!
		if (sessionId == null) 
			throw new VariantRuntimeException(MessageTemplate.RUN_SESSION_ID_NULL, sidTracker.getClass().getSimpleName());

		// Get the session by ID from the session store.  NULL if desn't exist or expired.
		VariantSessionImpl result = (VariantSessionImpl) sessionStore.get(sessionId);

		// If none, create new and save in store.
		if (result == null) {
			VariantTargetingTracker targetingTracker = initTargetingTracker(userData);
			result = new VariantSessionImpl(client, sidTracker, targetingTracker);
			sessionStore.save(result);
		}
		
		return result;	
	}
	
	/**
	 * Persist user session in session store.
	 * @param session
	 * TODO Make this async
	 */
	public void saveSession(VariantSession session, Object...userData) {
		if (client.getSchema() == null) throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_UNDEFINED);
		if (!client.getSchema().getId().equals(session.getSchemaId())) 
			throw new VariantRuntimeException(MessageTemplate.RUN_SCHEMA_REPLACED, client.getSchema().getId(), session.getSchemaId());
		sessionStore.save(session);
	}
}
