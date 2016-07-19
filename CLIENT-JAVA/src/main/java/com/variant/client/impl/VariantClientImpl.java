package com.variant.client.impl;

import static com.variant.client.VariantClientPropertyKeys.SESSION_ID_TRACKER_CLASS_INIT;
import static com.variant.client.VariantClientPropertyKeys.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.VariantClientPropertyKeys.TARGETING_TRACKER_CLASS_INIT;
import static com.variant.client.VariantClientPropertyKeys.TARGETING_TRACKER_CLASS_NAME;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_SESSION_ID_TRACKER_NO_INTERFACE;
import static com.variant.core.schema.impl.MessageTemplate.BOOT_TARGETING_TRACKER_NO_INTERFACE;
import static com.variant.core.schema.impl.MessageTemplate.RUN_SCHEMA_UNDEFINED;

import java.io.InputStream;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantClient;
import com.variant.client.VariantClientPropertyKeys;
import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.session.ClientSessionCache;
import com.variant.core.VariantCorePropertyKeys.Key;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantProperties;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.exception.VariantSchemaModifiedException;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCore;
import com.variant.core.schema.Schema;
import com.variant.core.schema.Test.OnState.Variant;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.session.SessionStore;
import com.variant.core.util.VariantStringUtils;

/**
 * <p>Variant Java Client API. Makes no assumptions about the host application other than 
 * it is Java (can compile with Java). 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class VariantClientImpl implements VariantClient {
	
	private static final Logger LOG = LoggerFactory.getLogger(VariantClientImpl.class);
	private static final Random RAND = new Random(System.currentTimeMillis());

	private VariantCore core = null;
	private VariantProperties properties = null;
	private SessionStore sessionStore = null;
	private ClientSessionCache cache = null;
	
	/**
	 * Instantiate session ID tracker.
	 * TODO: reflective object creation is expensive.
	 * @param userData
	 * @return
	 */
	private VariantSessionIdTracker initSessionIdTracker() {
		// Session ID tracker.
		String sidTrackerClassName = properties.get(SESSION_ID_TRACKER_CLASS_NAME, String.class);
		try {
			Class<?> sidTrackerClass = Class.forName(sidTrackerClassName);
			Object sidTrackerObject = sidTrackerClass.newInstance();
			if (sidTrackerObject instanceof VariantSessionIdTracker) {
				VariantSessionIdTracker result = (VariantSessionIdTracker) sidTrackerObject;
				VariantInitParamsImpl initParams = new VariantInitParamsImpl(this, SESSION_ID_TRACKER_CLASS_INIT);
				result.initialized(initParams);
				return result;
			}
			else {
				throw new VariantBootstrapException(BOOT_SESSION_ID_TRACKER_NO_INTERFACE, sidTrackerClassName, SessionStore.class.getName());
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
	private VariantTargetingTracker initTargetingTracker() {
		
		// Instantiate targeting tracker.
		String className = properties.get(TARGETING_TRACKER_CLASS_NAME, String.class);
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof VariantTargetingTracker) {
				VariantTargetingTracker result = (VariantTargetingTracker) object;
				VariantInitParamsImpl initParams = new VariantInitParamsImpl(this, TARGETING_TRACKER_CLASS_INIT);
				result.initialized(initParams);
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
	 */
	public VariantClientImpl(String...resourceNames) {
		
		core = new VariantCore(resourceNames);
		core.getComptime().registerComponent(VariantComptime.Component.CLIENT, "0.6.1");		
		properties = core.getProperties();


		if (LOG.isDebugEnabled()) {
			LOG.debug("+-- Bootstrapping Variant Client with following application properties: --");
			for (Key key: Key.keys(VariantClientPropertyKeys.class)) {
				LOG.debug("| " + key.propertyName() + " = " + properties.get(key, String.class) + " : " + properties.getSource(key));
			}
			LOG.debug("+------------- Fingers crossed, this is not PRODUCTION -------------");
		}
	}

	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link CoreProperties} type.
	 * 
	 * @since 0.6
	 */
	@Override
	public VariantProperties getProperties() {
		return properties;
	}

	/**
	 * <p>Register a {@link HookListener}.
	 * See {@link Variant#addHookListener(HookListener)} for details.
	 * 
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link com.variant.core.hook.HookListener} interface.
	 *        
	 * @since 0.6
	 */
	@Override
	public void addHookListener(HookListener<?> listener) {
		core.addHookListener(listener);
	}
	
	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 */
	@Override
	public void clearHookListeners() {
		core.clearHookListeners();
	}

	/**
	 * <p>Parse and, if no errors, optionally deploy a new experiment schema.
	 * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 * @param deploy The new test schema will be deployed if this is true and no parse errors 
	 *        were encountered.
	 *        
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} object that
	 *         may be further examined about the outcome of this operation.
	 * 
	 * @since 0.5
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {		
		return core.parseSchema(stream, deploy);
	}

	/**
	 * <p>Parse and, if no errors, deploy a new experiment schema.  Same as 
     * <code>parseSchema(stream, true)</code>.
     * 
	 * @param stream The schema to be parsed and deployed, as a java.io.InputStream.
	 *         
	 * @return An instance of the {@link com.variant.core.schema.parser.ParserResponse} object, which
	 *         may be further examined about the outcome of this operation.
     *
	 * @since 0.5
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream) {
		return core.parseSchema(stream);
	}

	/**
	 * <p>Get currently deployed test schema, if any.
	 * 
	 * @return Current test schema as an instance of the {@link com.variant.core.schema.Schema} object.
	 * 
	 * @since 0.5
	 */
	@Override
	public Schema getSchema() {
		return core.getSchema();
	}

	/**
	 */
	@Override
	public VariantSession getSession(boolean create, Object... userData) {
		
		// Get session ID from the session ID tracker.
		VariantSessionIdTracker sidTracker = initSessionIdTracker();
		String sessionId = sidTracker.get(userData);
		if (sessionId == null) {
			if (create) {
				sessionId = VariantStringUtils.random64BitString(RAND);
			}
			else {
				// No ID in the tracker and create wasn't given. Same as expired session.
				return null;
			}
		}
		
		// Have session ID. Try the local cache first.
		VariantSession ssnFromCache = cache.get(sessionId);
		if (ssnFromCache == null) {
			if (create) {
				// Session expired locally, recreate OK.  Don't bother with the server.
				VariantCoreSession coreSession = new CoreSessionImpl(sessionId, core);
				core.saveSession(coreSession);
				VariantSessionImpl clientSession = new VariantSessionImpl(coreSession, sidTracker, initTargetingTracker());
				cache.put(clientSession);
				return clientSession;
			}
			else {
				// Session expired locally, recreate not OK.
				return null;
			}
		}		
		
		// If we had local session, attempt to get the core from the server.
		VariantCoreSession ssnFromStore = core.getSession(sessionId, create);
		
		if (ssnFromStore == null) {
			// Session expired on server => expire it here too.
			cache.expire(sessionId);
			if (create) {
				// Recreate from scratch
				VariantCoreSession coreSession = new CoreSessionImpl(sessionId, core);
				core.saveSession(coreSession);
				VariantSessionImpl clientSession = new VariantSessionImpl(coreSession, sidTracker, initTargetingTracker());
				cache.put(clientSession);
				return clientSession;
			}
			else {
				// Do not recreate.
				return null;
			}
		}
		else {
			// Have both sessions, local and on server.
			((VariantSessionImpl)ssnFromCache).replaceCoreSession(ssnFromCache);
			return ssnFromCache;	
		}
	}
			
	/**
	 * <p>Get user's Variant session.
	 * 
	 * @param httpRequest Current <code>HttpServletRequest</code>.
	 * @since 0.5
	 * @return
	 */
	@Override
	public VariantSession getSession(Object... userData) {
		return getSession(true, userData);
	}

	//---------------------------------------------------------------------------------------------//
	//                                      PUBLIC EXT                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * @return Core API Object.
	 * @since 0.5
	 */
	public VariantCore getCoreApi() {
		return core;
	}

	/**
	 * Save user session in session store.
	 * @param session
	 * TODO Make this async
	 */
	public void saveSession(VariantSession session, Object...userData) {
		if (getSchema() == null) throw new VariantRuntimeUserErrorException(RUN_SCHEMA_UNDEFINED);
		if (!getSchema().getId().equals(session.getSchemaId())) 
			throw new VariantSchemaModifiedException(getSchema().getId(), session.getSchemaId());
		sessionStore.save(session);
	}

}
