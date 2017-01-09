package com.variant.client.conn;

import static com.variant.client.ConfigKeys.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.ConfigKeys.TARGETING_TRACKER_CLASS_NAME;
import static com.variant.client.impl.ClientUserError.SESSION_ID_TRACKER_NO_INTERFACE;
import static com.variant.client.impl.ClientUserError.TARGETING_TRACKER_NO_INTERFACE;

import java.util.Random;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.SessionIdTracker;
import com.variant.client.TargetingTracker;
import com.variant.client.VariantClient;
import com.variant.client.impl.SessionImpl;
import com.variant.client.net.Payload;
import com.variant.client.session.SessionCache;
import com.variant.core.UserError.Severity;
import com.variant.core.VariantException;
import com.variant.core.impl.UserHooker;
import com.variant.core.schema.ParserMessage;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.SchemaParser;
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
	private void preChecks() {
		
		switch (status) {
		
		case OPEN: break;
		
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
	 * Instantiate targeting tracker.
	 * 
	 * @param userData
	 * @return
	 */
	private TargetingTracker initTargetingTracker(Object...userData) {
		
		// Instantiate targeting tracker.
		String className = client.getConfig().getString(TARGETING_TRACKER_CLASS_NAME);
		
		try {
			Object object = Class.forName(className).newInstance();
			if (object instanceof TargetingTracker) {
				TargetingTracker result = (TargetingTracker) object;
				result.init(this, userData);
				return result;
			}
			else {
				throw new ClientException.User(TARGETING_TRACKER_NO_INTERFACE, className, TargetingTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new ClientException.Internal("Unable to instantiate targeting tracker class [" + className +"]", e);
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
				sessionId = VariantStringUtils.random64BitString(RAND);
				sidTracker.set(sessionId);
			}
			else {
				// No ID in the tracker and create wasn't given. Same as expired session.
				return null;
			}
		}
		
		SessionImpl result = getSessionFromServer(sessionId);
		
		if (result == null && create) {
			// Session expired locally, recreate OK.  Don't bother with the server.
			CoreSession coreSession = new CoreSession(sessionId, schema);
			server.saveSession(coreSession);
			result = new SessionImpl(this, coreSession, sidTracker, initTargetingTracker(userData));
			cache.add(result);
		}

		return result;
	}
	/**
	 * Get an existing session by session ID from the server.
	 * @param create
	 * @param userData
	 * @return
	 */
	private SessionImpl getSessionFromServer(String sid) {
				
		// Local cache first.
		SessionImpl ssnFromCache = (SessionImpl) cache.get(sid);
		if (ssnFromCache == null) return null;
		
		// Local cache hit. Still have to hit the the server.
		Payload.Session payload = server.get(sid);
		
		if (payload == null) {
			// Session expired on server => expire it here too.
			cache.expire(sid);
			return null;			
		}
		
		CoreSession ssnFromStore =  payload.session;			
		// Local and remote hits. 
		// Replace remote in local, as it may have been changed by another client,
		// update local timeout, and return the existing local object.
		ssnFromCache.replaceCoreSession(ssnFromStore);
		cache.add(ssnFromCache);
		return ssnFromCache;	
	}


	private static final Random RAND = new Random(System.currentTimeMillis());
	
	private final String id;
	private final long sessionTimeoutMillis;
	private final long timestamp;
	private final SessionCache cache;
	private final Server server;
	private final VariantClient client; 
	private final Schema schema;
	private Status status = Status.OPEN;

	/**
	 * 
	 */
	ConnectionImpl(VariantClient client, String url) {
		this.client = client;
		
		// This connection's server object.
		this.server = new Server(this, url);
		
		// Get the schema from the server, if exists.
		Payload.Connection payload = server.connect();
		
		id = payload.id;
		timestamp = payload.timestamp;
		sessionTimeoutMillis = payload.sessionTimeout * 1000;
		cache = new SessionCache(sessionTimeoutMillis);
		
		ParserResponse resp = new SchemaParser(new UserHooker()).parse(payload.schemaSrc);
		if (resp.hasMessages(Severity.ERROR)) {
			StringBuilder buff = new StringBuilder("Unable to parse schema:\n");
			for (ParserMessage msg: resp.getMessages()) buff.append("    ").append(msg.toString()).append("\n");
			throw new VariantException(buff.toString());
		}
		schema = resp.getSchema();
	}
	
	// ---------------------------------------------------------------------------------------------//
	//                                             PUBLIC                                           //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	@Override
	public VariantClient getClient() {
		preChecks();
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
		return getSessionFromServer(sessionId);
	}
	
	@Override
	public Schema getSchema() {
		preChecks();
		return schema;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void close() {
		close(Status.CLOSED_BY_CLIENT);
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	public String getId() {
		return id;
	}

	/**
	 */
	public long getSessionTimeoutMillis() {
		return sessionTimeoutMillis;
	}

	public void close(Status status) {
		if (this.status == Status.OPEN) {
			server.disconnect(id);
			this.status = status;
		}		
	}
	
	/**
	 * This connection's unerlying server.
	 */
	public Server getServer() {
		return server;
	}
	
	/**
	 * JSON deserialization.
	 * @param parsedJson
	 * @return
	 *
	public static Connection fromJson(Map<String,?> parsedJson) {
		// Parse the schema.  We don't have user hooks on the client.
	    ParserResponse pr = new SchemaParser(null).parse(payload.getContent());
			val schema = ServerSchema(response)		
			if (response.hasMessages(Severity.ERROR)) schema.state = State.Failed
	}
*/
}
