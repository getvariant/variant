package com.variant.client.impl;

import static com.variant.client.impl.ClientUserError.SESSION_ID_TRACKER_NO_INTERFACE;
import static com.variant.client.impl.ConfigKeys.SESSION_ID_TRACKER_CLASS_NAME;

import java.util.ArrayList;
import java.util.Random;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.VariantClient;
import com.variant.client.lifecycle.ConnectionClosed;
import com.variant.client.lifecycle.LifecycleEvent;
import com.variant.client.lifecycle.LifecycleHook;
import com.variant.client.net.Payload;
import com.variant.client.session.SessionCache;
import com.variant.core.ConnectionStatus;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.session.CoreSession;
import com.variant.core.util.StringUtils;
import com.variant.core.util.immutable.ImmutableList;

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
	private void preChecks() {
		
		switch (status) {
		
		case OPEN:
		case DRAINING: break;
		
		case CLOSED_BY_CLIENT: 
		case CLOSED_BY_SERVER: throw new ConnectionClosedException(ClientUserError.CONNECTION_CLOSED);		
		}
	}
	
	/**
	 * Instantiate session ID tracker.
	 * 
	 * @param userData
	 * @return
	 */
	private SessionIdTracker initSessionIdTracker(Object...userData) {
		// Session ID tracker.
		String className = client.getConfig().getString(SESSION_ID_TRACKER_CLASS_NAME);
		try {
			Class<?> sidTrackerClass = Class.forName(className);
			Object sidTrackerObject = sidTrackerClass.newInstance();
			if (sidTrackerObject instanceof SessionIdTracker) {
				SessionIdTracker result = (SessionIdTracker) sidTrackerObject;
				result.init(this, userData);
				return result;
			}
			else {
				throw new ClientException.User(SESSION_ID_TRACKER_NO_INTERFACE, className, SessionIdTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new ClientException.Internal("Unable to instantiate session id tracker class [" + className + "]", e);
		}

	}
			
	/**
	 * 
	 * @param create
	 * @param userData
	 * @return null if session has expired and create is false; session object otherwise.
	 */
	private Session _getSession(boolean create, Object... userData) {
		
		// Get session ID from the session ID tracker.
		SessionIdTracker sidTracker = initSessionIdTracker(userData);
		String sessionId = sidTracker.get();
		if (sessionId == null) {
			if (create) {
				sessionId = StringUtils.random64BitString(RAND);
				sidTracker.set(sessionId);
			}
			else {
				// No ID in the tracker and create wasn't given. Same as expired session.
				return null;
			}
		}
		
		
		SessionImpl result = null;
		
		try {
			result = fetchSession(sessionId, null);
		}
		catch (SessionExpiredException sex) { } // Return null instead.
		
		// if Session wasn't found on the server and the connection is draining,
		// throw exception rather than return null.
		if (result == null && status == ConnectionStatus.DRAINING)
			throw new ConnectionClosedException(ClientUserError.CONNECTION_DRAINING);
		
		if (result == null && create) {
			// Session expired locally, recreate OK => Recreate locally and save to server.
			CoreSession coreSession = new CoreSession(sessionId);
			result = new SessionImpl(this, coreSession, sidTracker, userData);
			client.server.sessionSave(result);
			cache.add(result);
		}

		return result;
	}
	
	/**
	 * Refresh an existing session from the server.
	 * Pass through any exceptions.
	 * @param session
	 */
	void refreshSession(SessionImpl session) {
		fetchSession(session.getId(), session);
	}
	
	/**
	 * Get an existing session by session ID from the server.
	 * If session exists on the server
	 *   if exists locally then rewrap
	 *   else create new local session
	 * else
	 *   if exists locally then expire, return null.
	 *   else return null.
	 *   
	 * @return
	 */
	private SessionImpl fetchSession(String sid, SessionImpl localSession) {

		// Go straight to the server as we may not have it in the client-local cache,
		// e.g. when we get it from a parallel connection.
		Payload.Session payload = client.server.sessionGet(sid, this);

		CoreSession serverSsn =  payload.session;
		
		// Have server session. If no client-side session object, create it
		// otherwise, rewrap the core session from the server in the existing
		// client-side object.
		
		SessionImpl clientSsn = localSession == null ? (SessionImpl) cache.get(sid) : localSession;
		
		if (clientSsn == null) {
			// New headless session
			clientSsn = new SessionImpl(this, serverSsn);
			cache.add(clientSsn);
		}
		else {
			clientSsn.rewrap(serverSsn);
		}
		return clientSsn;	
	}

	// Lifecycle hooks.
	final private ArrayList<LifecycleHook<? extends LifecycleEvent>> lifecycleHooks = 
			new ArrayList<LifecycleHook<? extends LifecycleEvent>>();

	// Session cache has package visibilithy because accessed by HooksService.
	final SessionCache cache;


	//final private static Logger LOG = LoggerFactory.getLogger(ConnectionImpl.class);


	private static final Random RAND = new Random(System.currentTimeMillis());
	
	private final String id;
	private final long sessionTimeoutMillis;
	private final long timestamp;
	private final Schema schema;
	private ConnectionStatus status = null;

	final VariantClientImpl client;

	/**
	 * 
	 */
	ConnectionImpl(VariantClientImpl client, Payload.Connection payload) {
		
		this.client = client;
		
		id = payload.id;
		timestamp = payload.timestamp;
		sessionTimeoutMillis = payload.sessionTimeout * 1000;
		cache = new SessionCache(this);
		
		ParserResponse resp = new ClientSchemaParser().parse(payload.schemaSrc);
		if (resp.hasMessages(Severity.ERROR)) {
			StringBuilder buff = new StringBuilder("Unable to parse schema:\n");
			for (ParserMessage msg: resp.getMessages()) buff.append("    ").append(msg.toString()).append("\n");
			throw new ClientException.Internal(buff.toString());
		}
		status = ConnectionStatus.OPEN;
		schema = new SchemaImpl(payload.schemaId, resp.getSchema());
	}
	
	/**
	 * 
	 */
	private void destroy() {
		cache.destroy();
		client.freeConnection(id);
	}
	
	// ---------------------------------------------------------------------------------------------//
	//                                             PUBLIC                                           //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Should always be safe. We call this during open connection..
	 */
	@Override
	public VariantClient getClient() {
		//preChecks(); We need to call this while raising connection closed LCE.
		return client;
	}

	/**
	 */
	@Override
	public Session getOrCreateSession(Object... userData) {
		preChecks(); 
		return _getSession(true, userData);
	}
			
	/**
	 */
	@Override
	public Session getSession(Object... userData) {
		preChecks();
		return _getSession(false, userData);
	}

	@Override
	public Session getSessionById(String sessionId) {
		preChecks();
		// Intercept session expired exception.
		try {
			return fetchSession(sessionId, null);
		}
		catch (SessionExpiredException sex) { 
			return null;
		}	
	}
	
	@Override
	public Schema getSchema() {
		preChecks();
		return schema;
	}

	@Override
	public ConnectionStatus getStatus() {
		return status;
	}

	@Override
	public void addLifecycleHook(LifecycleHook<? extends LifecycleEvent> hook) {
		preChecks();
		lifecycleHooks.add(hook);
	}

	@Override
	public void close() {
		preChecks();
		client.server.disconnect(this);
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	public long getSessionTimeoutMillis() {
		return sessionTimeoutMillis;
	}

	/**
	 */
	public void setStatus(ConnectionStatus status) {
	
		if (status == this.status) return;
	
		switch (status) {
		
		case OPEN:
			throw new ClientException.Internal("Cannot reopen a connection (currently " + this.status + ")");				
		
		case DRAINING: 
			this.status = status;
			break; // Anything?

		case CLOSED_BY_SERVER:
			this.status = status;
			destroy();
			client.lceService.raiseEvent(ConnectionClosed.class, this);
			throw new ConnectionClosedException(ClientUserError.CONNECTION_CLOSED);
		
		case CLOSED_BY_CLIENT:
			this.status = status;
			destroy();
			client.lceService.raiseEvent(ConnectionClosed.class, this);
			break;
			
		default: 
			throw new ClientException.Internal("Unexpected connection status from server " + status + ")");
		}
	}
	
	/**
	 * Read-only snapshot.
	 */
	public ImmutableList<LifecycleHook<? extends LifecycleEvent>> getLifecycleHooks() {
		return new ImmutableList<LifecycleHook<? extends LifecycleEvent>>(lifecycleHooks);
	}
	
	/**
	 * Only tests are allowed to use this.
	 */
	public SessionCache getSessionCache() {
		return cache;
	}

}
