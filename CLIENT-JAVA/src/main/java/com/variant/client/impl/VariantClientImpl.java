package com.variant.client.impl;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.Properties;
import com.variant.client.VariantClient;
import com.variant.client.net.SessionPayloadReader;
import com.variant.client.session.SessionCache;
import com.variant.core.session.SessionStore;

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

	private final Properties properties = new SystemPropertiesImpl();
	private SessionStore sessionStore = null;
	private SessionCache cache = null;
	
	/**
	 * Handshake with the server.
	 * @param payloadReader
	 */
	private void handshake(SessionPayloadReader payloadReader) {
		// Nothing for now.
	}
	
	/**
	 *
	private VariantSession _getSession(boolean create, Object... userData) {
		
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
		
		if (ssnFromCache == null) {
			// Local case miss.
			if (create) {
				// Session expired locally, recreate OK.  Don't bother with the server.
				CoreSession coreSession = new CoreSessionImpl(sessionId);
				coreSession.save();
				VariantSessionImpl clientSession = new VariantSessionImpl(this, coreSession, sidTracker, initTargetingTracker(userData));
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
		CoreSession ssnFromStore = null;

		try {
			payloadReader = core.getSession(sessionId, create);
		}
		catch(VariantSchemaModifiedException e) {}

		// Ensure we are compatible with the server.
		if (payloadReader != null)	{
			handshake(payloadReader);
			ssnFromStore = (CoreSession) payloadReader.getBody();			
		}
		
		if (ssnFromStore == null) {
			// Session expired on server => expire it here too.
			cache.expire(sessionId);
			if (create) {
				// Recreate from scratch
				CoreSession coreSession = new CoreSession(sessionId, core);
				coreSession.save();
				VariantSessionImpl clientSession = new VariantSessionImpl(this, coreSession, sidTracker, initTargetingTracker(userData));
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
		((VariantSessionImpl)ssnFromCache).replaceCoreSession(ssnFromStore);
		cache.add(ssnFromCache, payloadReader.getProperty(Payload.Property.SSN_TIMEOUT, Long.class));
		return ssnFromCache;	
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClientImpl(String...resourceNames) { /* What happened to the resource names? */ }

	/**
	 * <p>This API's application properties
	 * 
	 * @return An instance of the {@link CoreProperties} type.
	 * 
	 * @since 0.6
	 */
	@Override
	public Properties getProperties() {
		return properties;
	}

	/**
	 * <p>Register a {@link HookListener}.
	 * See {@link Variant#addHookListener(HookListener)} for details.
	 * 
	 * @param listener An instance of a caller-provided implementation of the 
	 *        {@link com.variant.server.hook.HookListener} interface.
	 *        
	 * @since 0.6
	 *
	@Override
	public void addHookListener(HookListener<?> listener) {
		core.addHookListener(listener);
	}
	
	/**
	 * <p>Remove all previously registered (with {@link #addHookListener(HookListener)} listeners.
	 * 
	 * @since 0.5
	 *
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
	 * @return An instance of the {@link com.variant.server.ParserResponse} object that
	 *         may be further examined about the outcome of this operation.
	 * 
	 * @since 0.5
	 *
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
	 * @return An instance of the {@link com.variant.server.ParserResponse} object, which
	 *         may be further examined about the outcome of this operation.
     *
	 * @since 0.5
	 *
	@Override
	public ParserResponse parseSchema(InputStream stream) {
		return core.parseSchema(stream);
	}


	/**
	 *
	@Override
	public VariantSession getOrCreateSession(Object... userData) {
		return _getSession(true, userData);
	}
			
	/**
	 *
	@Override
	public VariantSession getSession(Object... userData) {
		return _getSession(false, userData);
	}
*/
	//---------------------------------------------------------------------------------------------//
	//                                      PUBLIC EXT                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 * @return Core API Object.
	 * @since 0.5
	 *
	public VariantCore getCoreApi() {
		return core;
	}

	/**
	 * Save user session in session store.
	 * @param session
	 * TODO Make this async
	 *
	public void saveSession(CoreSession session, Object...userData) {
		if (getSchema() == null) throw new VariantRuntimeUserErrorException(RUN_SCHEMA_UNDEFINED);
		if (!getSchema().getId().equals(session.getSchemaId())) 
			throw new VariantSchemaModifiedException(getSchema().getId(), session.getSchemaId());
		sessionStore.save(session);
	}
    */
}
