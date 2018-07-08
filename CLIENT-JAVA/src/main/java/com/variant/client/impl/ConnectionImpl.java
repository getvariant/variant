package com.variant.client.impl;

import static com.variant.client.ConfigKeys.SESSION_ID_TRACKER_CLASS_NAME;
import static com.variant.client.impl.ClientUserError.SESSION_ID_TRACKER_NO_INTERFACE;

import java.util.Random;

import com.variant.client.VariantException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.VariantClient;
import com.variant.client.net.Payload;
import com.variant.client.session.SessionCache;
import com.variant.core.UserError.Severity;
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


	private static final Random RAND = new Random(System.currentTimeMillis());
	
	private final long sessionTimeoutMillis;
	private final String schema;

	/**
	 * Anything?
	 */
	private void preChecks() {
		// Nothing?
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
				throw new VariantException(SESSION_ID_TRACKER_NO_INTERFACE, className, SessionIdTracker.class.getName());
			}
		}
		catch (Exception e) {
			throw new VariantException.Internal("Unable to instantiate session id tracker class [" + className + "]", e);
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
				// No ID in the tracker and create wasn't given.
				return null;
			}
		}
				
		try {
			return fetchSession(sessionId, create, null);
		}
		catch (SessionExpiredException sex) { 
			return null;
		}
	}
	
	/**
	 * Refresh an existing session from the server.
	 * Pass through any exceptions.
	 * @param session
	 */
	void refreshSession(SessionImpl session) {
		fetchSession(session.getId(), false, session);
	}
	
	/**
	 * Get or create session by ID from the server.
	 *   
	 * @return
	 */
	private SessionImpl fetchSession(String sid, boolean create, SessionImpl localSession) {

		// If we already have this session locally, we will reuse the schema.
		SessionImpl clientSsn = localSession == null ? (SessionImpl) cache.get(sid) : localSession;

		// This will throw exceptions if no session.	
		Payload.Session payload = create ? 
				client.server.sessionGetOrCreate(sid, this) : client.server.sessionGet(sid, this);
			
	    // Parse the schema, but only if don't already have it locally.
		Schema schema = null;
		if (clientSsn == null) {
			ParserResponse resp = new ClientSchemaParser().parse(payload.schemaSrc);
			if (resp.hasMessages(Severity.ERROR)) {
				StringBuilder buff = new StringBuilder("Unable to parse schema:\n");
				for (ParserMessage msg: resp.getMessages()) buff.append("    ").append(msg.toString()).append("\n");
				throw new VariantException.Internal(buff.toString());
			}
			schema = new SchemaImpl(payload.schemaId, resp.getSchema());
		}
		else {
			schema = clientSsn.getSchema();
		}
		
		// Can create the core session now
		CoreSession serverSsn =  CoreSession.fromJson(payload.coreSsnSrc, schema);
		
		// If no client-side session object, create it. 
		// Otherwise, simply replace the core session from the server.
		if (clientSsn == null) {
			clientSsn = new SessionImpl(this, schema, serverSsn);
			cache.add(clientSsn);
		}
		else {
			// This client has the session.
			clientSsn.rewrap(serverSsn);
		}
		return clientSsn;	
	}

	/*
	// Lifecycle hooks.
	final private ArrayList<LifecycleHook<? extends ClientLifecycleEvent>> lifecycleHooks = 
			new ArrayList<LifecycleHook<? extends ClientLifecycleEvent>>();
	*/

	// Session cache has package visibilithy because accessed by HooksService.
	final SessionCache cache;

	final VariantClientImpl client;
	

	/**
	 * 
	 */
	ConnectionImpl(VariantClientImpl client, String schema, Payload.Connection payload) {
		
		this.client = client;
		this.schema = schema;
		
		sessionTimeoutMillis = payload.sessionTimeout * 1000L;
		cache = new SessionCache(this);

	}
	
	/**
	 * Connections have no server side state
	 * 
	private void destroy() {
		cache.destroy();
		client.freeConnection(id);
	}
	*/
	// ---------------------------------------------------------------------------------------------//
	//                                             PUBLIC                                           //
	// ---------------------------------------------------------------------------------------------//

	/**
	 *
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
			return fetchSession(sessionId, false, null);
		}
		catch (SessionExpiredException sex) { 
			return null;
		}	
	}

	@Override
	public String getSchemaName() {
		return schema;
	}
/*
	@Override
	public ConnectionStatus getStatus() {
		return status;
	}

	@Override
	public void addLifecycleHook(LifecycleHook<? extends ClientLifecycleEvent> hook) {
		preChecks();
		lifecycleHooks.add(hook);
	}

	@Override
	public void close() {
		preChecks();
		client.server.disconnect(this);
	}
*/
	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	public long getSessionTimeoutMillis() {
		return sessionTimeoutMillis;
	}
	
	/**
	 * Read-only snapshot.
	 *
	public ImmutableList<LifecycleHook<? extends ClientLifecycleEvent>> getLifecycleHooks() {
		return new ImmutableList<LifecycleHook<? extends ClientLifecycleEvent>>(lifecycleHooks);
	}
	
	/**
	 * Only tests are allowed to use this.
	 */
	public SessionCache getSessionCache() {
		return cache;
	}

}
