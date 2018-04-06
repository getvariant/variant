package com.variant.client.impl;

import static com.variant.client.ConfigKeys.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.impl.ClientUserError.SESSION_ID_TRACKER_NO_INTERFACE;

import java.util.LinkedHashSet;
import java.util.Random;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionIdTracker;
import com.variant.client.VariantClient;
import com.variant.client.net.Payload;
import com.variant.client.session.SessionCache;
import com.variant.core.ConnectionStatus;
import com.variant.core.UserError.Severity;
import com.variant.core.VariantException;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.session.CoreSession;
import com.variant.core.util.StringUtils;

/**
 * A connection to the server.
 * 
 * @author Igor
 */
public class ConnectionImpl implements Connection {

	//final private static Logger LOG = LoggerFactory.getLogger(ConnectionImpl.class);

	// Listeners
	final protected LinkedHashSet<ExpirationListener> expirationListeners = new LinkedHashSet<ExpirationListener>();

	/**
	 * Is this connection still valid?
	 * @return
	 */
	private void preChecks() {
		
		switch (status) {

		case CONNECTING:
			throw new ClientException.Internal("Connection is not yet open");
		
		case OPEN:
			break;
		
		case CLOSED_BY_CLIENT: 
		case CLOSED_BY_SERVER:
			throw new ConnectionClosedException();		
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
	 * @return
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
		
		SessionImpl result = fetchSession(sessionId, null);
		
		if (result == null && create) {
			// Session expired locally, recreate OK.  Don't bother with the server.
			CoreSession coreSession = new CoreSession(sessionId);
			result = new SessionImpl(this, coreSession, sidTracker, userData);
			server.sessionSave(result);
			cache.add(result);
		}

		return result;
	}
	
	/**
	 * Refresh an existing session from the server.
	 * @param session
	 */
	void refreshSession(SessionImpl session) {
		fetchSession(session.getId(), session);
	}
	
	/**
	 * Get an existing session by session ID from the server, or null if
	 * the session does not exist on the server.
	 * @param create
	 * @param userData
	 * @return
	 */
	private SessionImpl fetchSession(String sid, SessionImpl localSession) {

		// Go straight to the server as we may not have it in the client-local cache,
		// e.g. when we get it from a parallel connection.
		Payload.Session payload = server.sessionGet(sid);
		
		if (payload == null) {
			// Session expired on server, expire it here too.
			cache.expire(sid);
			return null;			
		}

		CoreSession serverSsn =  payload.session;
		
		// Have server session. If no client-side session object, create it
		// otherwise, rewrap the core session from the server in the existing
		// client-side object.
		
		SessionImpl clientSsn = localSession == null ? (SessionImpl) cache.get(sid) : localSession;
		
		if (clientSsn == null) {
			clientSsn = new SessionImpl(this, serverSsn);
		}
		else {
			clientSsn.rewrap(serverSsn);
		}
		return clientSsn;	
	}


	private static final Random RAND = new Random(System.currentTimeMillis());
	
	private final String id;
	private final long sessionTimeoutMillis;
	private final long timestamp;
	private final SessionCache cache;
	private final Server server;
	private final VariantClientImpl client; 
	private final Schema schema;
	private ConnectionStatus status = ConnectionStatus.CONNECTING;

	/**
	 * 
	 */
	ConnectionImpl(VariantClient client, String schemaName) {
		this.client = (VariantClientImpl) client;
		
		// This connection's server object.
		this.server = new Server(this, schemaName);
		
		// Get the schema from the server, if exists.
		Payload.Connection payload = server.connect();
		
		id = payload.id;
		timestamp = payload.timestamp;
		sessionTimeoutMillis = payload.sessionTimeout * 1000;
		cache = new SessionCache();
		
		ParserResponse resp = new ClientSchemaParser().parse(payload.schemaSrc);
		if (resp.hasMessages(Severity.ERROR)) {
			StringBuilder buff = new StringBuilder("Unable to parse schema:\n");
			for (ParserMessage msg: resp.getMessages()) buff.append("    ").append(msg.toString()).append("\n");
			throw new VariantException(buff.toString());
		}
		schema = resp.getSchema();
		
		status = ConnectionStatus.OPEN;
	}
	
	// ---------------------------------------------------------------------------------------------//
	//                                             PUBLIC                                           //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	@Override
	public String getId() {
		preChecks();
		return id;
	}

	/**
	 * Should always be safe. We call this during open connection..
	 */
	@Override
	public VariantClient getClient() {
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
		return fetchSession(sessionId, null);
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
	public void registerExpirationListener(ExpirationListener listener) {
		preChecks();
		expirationListeners.add(listener);
	}

	@Override
	public void close() {
		if (status == ConnectionStatus.OPEN) close(ConnectionStatus.CLOSED_BY_CLIENT);
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	public long getSessionTimeoutMillis() {
		return sessionTimeoutMillis;
	}

	public void close(ConnectionStatus status) {
		if (this.status == ConnectionStatus.OPEN) {
			cache.destroy();
			server.disconnect(id);
			client.freeConnection(id);
			this.status = status;
		}
	}
	
	/**
	 * This connection's unerlying server.
	 */
	public Server getServer() {
		return server;
	}	
}
