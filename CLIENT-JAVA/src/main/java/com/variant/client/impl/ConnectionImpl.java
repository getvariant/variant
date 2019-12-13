package com.variant.client.impl;

import static com.variant.client.VariantError.*;
import static com.variant.client.impl.ConfigKeys.SESSION_ID_TRACKER_CLASS;
import static com.variant.client.impl.ConfigKeys.TARGETING_TRACKER_CLASS;

import java.util.Optional;
import java.util.Random;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.SessionIdTracker;
import com.variant.client.TargetingTracker;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.net.Payload;
import com.variant.client.util.MethodTimingWrapper;
import com.variant.core.error.UserError.Severity;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserMessage;
import com.variant.core.schema.parser.ParserResponse;
import com.variant.core.session.CoreSession;
import com.variant.core.util.ReflectUtils;
import com.variant.core.util.StringUtils;
import com.variant.core.util.Tuples.Pair;

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
	final VariantClientImpl client;

	/**
	 * 
	 */
	public ConnectionImpl(VariantClientImpl client, String schema, Payload.Connection payload) {
		
		this.client = client;
		this.schema = schema;
		
		sessionTimeoutMillis = payload.sessionTimeout * 1000L;

	}
	
	/**
	 * Instantiate session ID tracker.
	 * 
	 * @param userData
	 * @return
	 */
	private SessionIdTracker initSessionIdTracker(Object...userData) {
		
		@SuppressWarnings("unchecked")
		Class<SessionIdTracker> klass = (Class<SessionIdTracker>) client.props.get(SESSION_ID_TRACKER_CLASS);
		try {
			return (SessionIdTracker) ReflectUtils.instantiate(klass, Object[].class, userData);
		}
		catch (Exception e) {
			throw new VariantException(SESSION_ID_TRCKER_INSTANTIATION_ERROR, e, klass.getName());
		}
	}
	
	/**
	 * Instantiate targeting tracker.
	 * 
	 * @param userData
	 * @return
	 */
	private TargetingTracker initTargetingTracker(Object...userData) {
		
		@SuppressWarnings("unchecked")
		Class<TargetingTracker> klass = (Class<TargetingTracker>) client.props.get(TARGETING_TRACKER_CLASS);
		try {
			return (TargetingTracker) ReflectUtils.instantiate(klass, Object[].class, userData);
		}
		catch (Exception e) {
			throw VariantException.internal("Unable to instantiate targeting tracker class [" + klass.getName() +"]", e);
		}

	}

			
	/**
	 * Get or create a foreground session from the server. Foreground sessions
	 * are backed by SID and targeting tracker implementations.  Does not throw
	 * SessionExpiredException
	 * 
	 * @param create
	 * @param userData
	 * @return null if session has expired and create is false; session object otherwise.
	 */
	private Session getForegroundSession(boolean create, Object... userData) {
				
		// Get session ID from the session ID tracker.
		SessionIdTracker sidTracker = initSessionIdTracker(userData);
		TargetingTracker targetingTracker = initTargetingTracker(userData);
		String sessionId = sidTracker.get();
		if (sessionId == null) {
			if (create) {
				sessionId = StringUtils.random64BitString(RAND);
			}
			else {
				// No ID in the tracker and create wasn't given.
				return null;
			}
		}
			
		try {
			Pair<CoreSession, Schema> fromServer = fetchSession(sessionId, create, targetingTracker);
			SessionImpl result = new SessionImpl(this, fromServer._2(), fromServer._1());
			// If the server returned a different SID, re-set the SID tracker.
			// This can only happen if we were called with create=true.
			if (!create && !result.getId().equals(sessionId)) {
				throw VariantException.internal("New SID not expected");
			}
			// Use the SID that came from the server, not the one we sent, because
			// if what we sent wasn't found on the server and we asked to create,
			// the server will generate a new SID — that's how we know it wasn't found --
			// see previous comment.
			sidTracker.set(result.getId());
			result.sessionIdTracker = sidTracker;
			result.targetingTracker = targetingTracker;
			return result;
		}
		catch (SessionExpiredException sex) { 
			return null;
		}
	}
	
	/**
	 * Get a headless session from the server. Not backed by SID or targeting trackers.
	 * Does not throw SessionExpiredException.
	 * 
	 * @param sid
	 * @return The session or null if did not exist.
	 */
	private Session getHeadlessSession(String sid) {
		try {
			Pair<CoreSession, Schema> fromServer = fetchSession(sid, false);
			return new SessionImpl(this, fromServer._2(), fromServer._1());
		}
		catch (SessionExpiredException sex) { 
			return null;
		}	
	}
		
	private Pair<CoreSession, Schema> fetchSession(String sid, boolean create) {
		return fetchSession(sid, create, TargetingTracker.empty());
	}

	/**
	 * Fetch a session from the server by its SID. Optionally, create a new one
	 * if session with the given SID didn't exist. If created, new session will
	 * have a different SID, created by the server.
	 * 
	 * Called by both implicit session refresh and explicit session get.
	 *   
	 * @return
	 */
	private Pair<CoreSession, Schema> fetchSession(String sid, boolean create, TargetingTracker tt) {

		// This may throw session expired exception.	
		Payload.Session payload = create ? 
				client.server.sessionGetOrCreate(sid, this, tt) : client.server.sessionGet(sid, this);
		
	    // Parse the schema.
		// TODO: cache parsed schema indexed by gen IDs, so as not to reparse on each session refresh.
		ParserResponse resp = new ClientSchemaParser().parse(payload.schemaSrc);
		if (resp.hasMessages(Severity.ERROR)) {
			StringBuilder buff = new StringBuilder("Unable to parse schema:\n");
			for (ParserMessage msg: resp.getMessages()) buff.append("    ").append(msg.toString()).append("\n");
			throw VariantException.internal(buff.toString());
		}
		Schema schema = new SchemaImpl(payload.schemaId, resp.getSchema());
		
		// Can create the core session now
		CoreSession serverSsn =  CoreSession.fromJson(payload.coreSsnSrc, schema);
		
		return new Pair<CoreSession, Schema>(serverSsn, schema);

	}

	/**
	 * Implicit refresh of an existing session from the server.
	 * Pass through any exceptions. 
	 * Package visibility.
	 * @param session
	 */
	void refreshSession(SessionImpl session) {
		Pair<CoreSession, Schema> fromServer = fetchSession(session.getId(), false);
		session.rewrap(fromServer._1());
	}

	// ---------------------------------------------------------------------------------------------//
	//                                             PUBLIC                                           //
	// ---------------------------------------------------------------------------------------------//

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

		return new MethodTimingWrapper<Session>().exec( () -> {
			return getForegroundSession(true, userData);
		});
	}
			
	/**
	 */
	@Override
	public Optional<Session> getSession(Object... userData) {
		return new MethodTimingWrapper<Optional<Session>>().exec( () -> {
			return Optional.ofNullable(getForegroundSession(false, userData));
		});
	}

	@Override
	public Optional<Session> getSessionById(String sid) {
		return new MethodTimingWrapper<Optional<Session>>().exec( () -> {
			return Optional.ofNullable(getHeadlessSession(sid));
		});
	}

	@Override
	public String getSchemaName() {
		return schema;
	}

	// ---------------------------------------------------------------------------------------------//
	//                                           PUBLIC EXT                                         //
	// ---------------------------------------------------------------------------------------------//

	/**
	 */
	public long getSessionTimeoutMillis() {
		return sessionTimeoutMillis;
	}
	
}
