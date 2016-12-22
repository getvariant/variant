package com.variant.client.impl;

import static com.variant.client.ConfigKeys.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.ConfigKeys.TARGETING_TRACKER_CLASS_NAME;

import java.util.Map;
import java.util.Random;

import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.net.Server;
import com.variant.client.net.SessionPayloadReader;
import com.variant.client.session.SessionCache;
import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.schema.Schema;
import com.variant.core.session.CoreSession;
import com.variant.core.util.VariantStringUtils;

/**
 * A connection to the server.
 * 
 * @author Igor
 */
public class ConnectionImpl implements Connection {

	/**
	 * Is this connection still valid?
	 * @return
	 */
	private void assertValid() {
		
		switch (status) {
		
		case OPEN: break;
		
		case CLOSED_BY_CLIENT: 
		case CLOSED_BY_SERVER:
			throw new ClientErrorException(ClientError.CONNECTION_CLOSED);
		}
	}
	
	/**
	 * Instantiate session ID tracker.
	 * TODO: reflective object creation is expensive.
	 * @param userData
	 * @return
	 */
	private VariantSessionIdTracker initSessionIdTracker(Object...userData) {
		// Session ID tracker.
		String className = client.getConfig().getString(SESSION_ID_TRACKER_CLASS_NAME);
		try {
			Class<?> sidTrackerClass = Class.forName(className);
			Object sidTrackerObject = sidTrackerClass.newInstance();
			if (sidTrackerObject instanceof VariantSessionIdTracker) {
				VariantSessionIdTracker result = (VariantSessionIdTracker) sidTrackerObject;
				result.init(client, userData);
				return result;
			}
			else {
				throw new ClientErrorException(ClientError.SESSION_ID_TRACKER_NO_INTERFACE, className, VariantSessionIdTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new RuntimeInternalException("Unable to instantiate session id tracker class [" + className + "]", e);
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
		String className = client.getConfig().getString(TARGETING_TRACKER_CLASS_NAME);
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof VariantTargetingTracker) {
				VariantTargetingTracker result = (VariantTargetingTracker) object;
				result.init(client, userData);
				return result;
			}
			else {
				throw new ClientErrorException(ClientError.TARGETING_TRACKER_NO_INTERFACE, className, VariantTargetingTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new RuntimeInternalException("Unable to instantiate targeting tracker class [" + className +"]", e);
		}
	}
	
	/**
	 * 
	 * @param create
	 * @param userData
	 * @return
	 */
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
		VariantSessionImpl ssnFromCache = (VariantSessionImpl) cache.get(sessionId);
		
		if (ssnFromCache == null) {
			// Local case miss.
			if (create) {
				// Session expired locally, recreate OK.  Don't bother with the server.
				CoreSession coreSession = new CoreSession(sessionId, schema);
				server.saveSession(this, coreSession);
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
		SessionPayloadReader payloadReader = server.get(this, sessionId);
		CoreSession ssnFromStore = null;
		
		if (payloadReader != null)	{
			ssnFromStore = (CoreSession) payloadReader.getContent();			
		}
		else {
			// Session expired on server => expire it here too.
			cache.expire(sessionId);
			if (create) {
				// Recreate from scratch
				CoreSession coreSession = new CoreSession(sessionId, schema);
				server.saveSession(this, coreSession);
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
		ssnFromCache.replaceCoreSession(ssnFromStore);
		cache.add(ssnFromCache);
		return ssnFromCache;	
	}

	private static final Random RAND = new Random(System.currentTimeMillis());
	
	private final VariantClientImpl client;
	private final Schema schema;
	private final Server server;
	private final SessionCache cache;
	private Status status;
	
	/**
	 * 
	 */
	ConnectionImpl(VariantClient client, Schema schema) {
		this.client = (VariantClientImpl) client;
		this.schema = schema;
		this.server = this.client.getServer();
		this.cache = new SessionCache(this);
		status = Status.OPEN;
	}
	
	/**
	 */
	@Override
	public VariantClient getClient() {
		return client;
	}

	/**
	 */
	@Override
	public VariantSession getOrCreateSession(Object... userData) {
		return _getSession(true, userData);
	}
			
	/**
	 */
	@Override
	public VariantSession getSession(Object... userData) {
		return _getSession(false, userData);
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void close() {
		status = Status.CLOSED_BY_CLIENT;
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	/**
	 * Server side session timeout, seconds
	 */
	public int getSessionTimeout() {
		return 0;
	}

	/**
	 * JSON deserialization.
	 * @param parsedJson
	 * @return
	 */
	public static Connection fromJson(Map<String,?> parsedJson) {
		return null;
	}

}
