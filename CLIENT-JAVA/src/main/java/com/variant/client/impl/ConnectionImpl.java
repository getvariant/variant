package com.variant.client.impl;

import static com.variant.client.Properties.Property.SESSION_ID_TRACKER_CLASS_INIT;
import static com.variant.client.Properties.Property.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.Properties.Property.TARGETING_TRACKER_CLASS_INIT;
import static com.variant.client.Properties.Property.TARGETING_TRACKER_CLASS_NAME;

import java.util.Random;

import org.apache.http.HttpStatus;

import com.variant.client.Connection;
import com.variant.client.Properties;
import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.VariantTargetingTracker;
import com.variant.client.net.http.HttpClient;
import com.variant.client.net.http.HttpResponse;
import com.variant.client.net.http.VariantHttpClientException;
import com.variant.client.session.SessionCache;
import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.net.SessionPayloadReader;
import com.variant.core.schema.Schema;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionStore;
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
		String sidTrackerClassName = client.getProperties().get(SESSION_ID_TRACKER_CLASS_NAME, String.class);
		try {
			Class<?> sidTrackerClass = Class.forName(sidTrackerClassName);
			Object sidTrackerObject = sidTrackerClass.newInstance();
			if (sidTrackerObject instanceof VariantSessionIdTracker) {
				VariantSessionIdTracker result = (VariantSessionIdTracker) sidTrackerObject;
				result.init(client, userData);
				return result;
			}
			else {
				throw new ClientErrorException(ClientError.SESSION_ID_TRACKER_NO_INTERFACE, sidTrackerClassName, SessionStore.class.getName());
			}
		}
		catch (Exception e) {
			throw new RuntimeInternalException("Unable to instantiate session id tracker class [" + sidTrackerClassName + "]", e);
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
		String className = client.getProperties().get(TARGETING_TRACKER_CLASS_NAME, String.class);
		
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
		VariantSession ssnFromCache = SessionCache.get(sessionId);
		
		if (ssnFromCache == null) {
			// Local case miss.
			if (create) {
				// Session expired locally, recreate OK.  Don't bother with the server.
				CoreSession coreSession = new CoreSession(sessionId, schema);
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

	private static final Random RAND = new Random(System.currentTimeMillis());
	
	private VariantClient client;
	private Status status;
	private Schema schema;
	
	/**
	 * 
	 */
	ConnectionImpl(VariantClient client, Schema schema) {
		this.client = client;
		this.schema = schema;
		status = Status.OPEN;
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

	/**
	 * Get a session 
	 * Recreate if does not exist and <code>create</code> is true.
	 * @return 
	 */
	public SessionPayloadReader getSession(String id, boolean create) throws VariantRuntimeException {
		
		assertValid();
		
		if (id == null || id.length() == 0) {
			throw new IllegalArgumentException("No session ID");
		}

		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(apiEndpointUrl + "session/" + sessionId);

		if (resp.getStatus() == HttpStatus.SC_OK) {
			return new SessionPayloadReader(coreApi, resp.getBody());
		}
		else if (resp.getStatus() == HttpStatus.SC_NO_CONTENT) {
			if (create) {
				CoreSession newSession = new CoreSession(sessionId, coreApi);
				save(newSession);
				return new SessionPayloadReader(coreApi, newSession.toJson());
			}
			else {
				return null;
			}
		}
		else {
			throw new VariantHttpClientException(resp);
		}

	}
	
	/**
	 * Persist user session in session store.
	 * @param session
	 * TODO Make this async
	 */
	public void saveSession(CoreSession session) {
		
		if (core.getSchema() == null) throw new VariantRuntimeUserErrorException(Error.RUN_SCHEMA_UNDEFINED);
		
		if (!core.getSchema().getId().equals(session.getSchemaId())) 
			throw new VariantRuntimeUserErrorException(Error.RUN_SCHEMA_MODIFIED, core.getSchema().getId(), session.getSchemaId());
		
		sessionStore.save(session);
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


}