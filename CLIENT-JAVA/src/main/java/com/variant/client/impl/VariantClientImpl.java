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

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantClient;
import com.variant.client.VariantClientPropertyKeys;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.session.ClientSessionCache;
import com.variant.client.session.ClientSessionImpl;
import com.variant.core.VariantCorePropertyKeys.Key;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.exception.VariantBootstrapException;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.exception.VariantSchemaModifiedException;
import com.variant.core.hook.HookListener;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCore;
import com.variant.core.net.Payload;
import com.variant.core.net.SessionPayloadReader;
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
	private VariantSessionIdTracker initSessionIdTracker(Object...userData) {
		// Session ID tracker.
		String sidTrackerClassName = properties.get(SESSION_ID_TRACKER_CLASS_NAME, String.class);
		try {
			Class<?> sidTrackerClass = Class.forName(sidTrackerClassName);
			Object sidTrackerObject = sidTrackerClass.newInstance();
			if (sidTrackerObject instanceof VariantSessionIdTracker) {
				VariantSessionIdTracker result = (VariantSessionIdTracker) sidTrackerObject;
				VariantInitParamsImpl initParams = new VariantInitParamsImpl(this, SESSION_ID_TRACKER_CLASS_INIT);
				result.init(initParams, userData);
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
	private VariantTargetingTracker initTargetingTracker(Object...userData) {
		
		// Instantiate targeting tracker.
		String className = properties.get(TARGETING_TRACKER_CLASS_NAME, String.class);
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof VariantTargetingTracker) {
				VariantTargetingTracker result = (VariantTargetingTracker) object;
				VariantInitParamsImpl initParams = new VariantInitParamsImpl(this, TARGETING_TRACKER_CLASS_INIT);
				result.init(initParams, userData);
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
	
	/**
	 * Handshake with the server.
	 * @param payloadReader
	 */
	private void handshake(SessionPayloadReader payloadReader) {
		// Nothing for now.
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClientImpl(String...resourceNames) {
		
		long now = System.currentTimeMillis();
		
		core = new VariantCore(resourceNames);
		core.getComptime().registerComponent(VariantComptime.Component.CLIENT, "0.6.1");		
		properties = core.getProperties();
		cache = new ClientSessionCache();

		if (LOG.isDebugEnabled()) {
			LOG.debug("+-- Bootstrapping Variant Client with following application properties: --");
			for (Key key: Key.keys(VariantClientPropertyKeys.class)) {
				LOG.debug("| " + key.propertyName() + " = " + properties.get(key, String.class) + " : " + properties.getSource(key));
			}
			LOG.debug("+------------- Fingers crossed, this is not PRODUCTION -------------");
		}

		LOG.info(String.format(
				"%s relese %s © 2015-16 getvariant.com. Bootstrapped in %s.", 
				core.getComptime().getComponent(),
				core.getComptime().getComponentVersion(),
				DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")));
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
		VariantSessionIdTracker sidTracker = initSessionIdTracker(userData);
		String sessionId = sidTracker.get();
		if (sessionId == null) {
			if (create) {
				sessionId = VariantStringUtils.random64BitString(RAND);
				sidTracker.set(sessionId);
			}
			else {
				// No ID in the tracker and create wasn't given. Same as expired session.
				return null;
			}
		}
		
		// Have session ID. Try the local cache first.
		VariantSession ssnFromCache = cache.get(sessionId);
		
		// Local case miss.
		if (ssnFromCache == null) {
			if (create) {
				// Session expired locally, recreate OK.  Don't bother with the server.
				CoreSessionImpl coreSession = new CoreSessionImpl(sessionId, core);
				coreSession.save();
				ClientSessionImpl clientSession = new ClientSessionImpl(coreSession, sidTracker, initTargetingTracker(userData));
				cache.add(clientSession);
				return clientSession;
			}
			else {
				// Session expired locally, recreate not OK.
				return null;
			}
		}		
		
		// Local cache hit. Try the the server.
		// If session exists remotely, but the schema has changed, ignore the remote version.
		SessionPayloadReader payloadReader = null;
		CoreSessionImpl ssnFromStore = null;

		try {
			payloadReader = core.getSession(sessionId, create);
		}
		catch(VariantSchemaModifiedException e) {}

		// Ensure we are compatible with the server.
		if (payloadReader != null)	{
			handshake(payloadReader);
			ssnFromStore = (CoreSessionImpl) payloadReader.getBody();			
		}
		
		if (ssnFromStore == null) {
			// Session expired on server => expire it here too.
			cache.expire(sessionId);
			if (create) {
				// Recreate from scratch
				CoreSessionImpl coreSession = new CoreSessionImpl(sessionId, core);
				coreSession.save();
				ClientSessionImpl clientSession = new ClientSessionImpl(coreSession, sidTracker, initTargetingTracker(userData));
				cache.add(clientSession);
				return clientSession;
			}
			else {
				// Do not recreate.
				return null;
			}
		}
		
		// Local and remote hits. 
		// Replace remote in local, as it may have been changed by another client,
		// update local timeout, and return the existing local object.
		((ClientSessionImpl)ssnFromCache).replaceCoreSession(ssnFromStore);
		cache.add(ssnFromCache, payloadReader.getProperty(Payload.Property.SSN_TIMEOUT, Long.class));
		return ssnFromCache;	
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
