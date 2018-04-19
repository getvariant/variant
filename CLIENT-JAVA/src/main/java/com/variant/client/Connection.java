package com.variant.client;

import com.variant.core.ConnectionStatus;
import com.variant.core.schema.Schema;


/**
 * <p>Represents a connection to Variant server. The first operation a new Variant client instance must
 * do is to connect to a particular schema on a Variant server, whose URL is provided by the
 * {@link ConfigKeys#SERVER_URL} config key.
 * 
 * <p>Not thread safe, avoid sharing a connection object between threads.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public interface Connection {
	
	/**
     * <p>The Variant client instance, which created this connection. 
     *  
	 * @return An instance of the {@link VariantClient} object, which originally created this object
	 *         via one of the {@code getSession()} calls.
	 *
	 * @since 0.7
	 */
	VariantClient getClient();

	/**
     * <p>This connection's unique ID. 
     *  
	 * @return Connection ID.
	 *
	 * @since 0.7
	 */
	String getId();

	/**
	 * Get, if exists, or create, if does not exist, the Variant session with the externally tracked ID.
	 * 
	 * Under normal circumstances, when this connection is {@link Status#OPEN}, the following behavior
	 * is expected. If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned. 
	 * Otherwise, a new session with this ID is created.
	 * 
	 * This method is idempotent, i.e. a subsequent calls with the same parameters
	 * will return the same object, unless the session has expired between the calls,
	 * in which case a brand new object will be returned.
	 * 
	 * However, if this connection is {@link Status#DRAINING}, no new sessions can be created. Therefore, 
	 * If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned, but if the
	 * session with this ID has expired, {@link ConnectionClosedException} is thrown.
	 * 
	 * Finally, if this connection is {@link Status#CLOSED_BY_CLIENT} or {@link Status#CLOSED_BY_SERVER}, 
	 * {@link ConnectionClosedException} is thrown.
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed, without interpretation,
	 *                 to the implementations of {@link SessionIdTracker#init(Connection, Object...)}
	 *                 and {@link TargetingTracker#init(Connection, Object...)}.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}. Never returns <code>null</code>.
	 */
	Session getOrCreateSession(Object... userData);

	/**
	 * Get, if exists, the Variant session with the externally tracked ID.
	 * 
	 * Under normal circumstances, when this connection is {@link Status#OPEN}, the following behavior
	 * is expected. If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned. 
	 * Otherwise, this method returns <code>null</code>.
	 * 
	 * This method is idempotent, i.e. a subsequent calls with the same parameters
	 * will return the same object, unless the session has expired between the calls,
	 * in which case a brand new object will be returned.
	 * 
	 * However, if this connection is {@link Status#DRAINING}, and the session with the ID 
	 * provided by the effective implementation of {@link SessionIdTracker} has not yet expired on the server, 
	 * it is returned. Otherwise, if the session with this ID has expired, {@link ConnectionClosedException}
	 * is thrown.
	 * 
	 * Finally, if this connection is {@link Status#CLOSED_BY_CLIENT} or {@link Status#CLOSED_BY_SERVER}, 
	 * {@link ConnectionClosedException} is thrown.
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link SessionIdTracker#init(Connection, Object...)}
	 *                 and {@link TargetingTracker#init(Connection, Object...)}.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}, if the session exists, or <code>null</code> otherwise.
	 */
	Session getSession(Object... userData);

	/**
	 * Get, if exists, the Variant session with the externally tracked ID.
	 * 
	 * Under normal circumstances, when this connection is {@link Status#OPEN}, the following behavior
	 * is expected. If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned. 
	 * Otherwise, this method returns <code>null</code>.
	 * 
	 * This method is idempotent, i.e. a subsequent calls with the same parameters
	 * will return the same object, unless the session has expired between the calls,
	 * in which case a brand new object will be returned.
	 * 
	 * However, if this connection is {@link Status#DRAINING}, and the session with the ID 
	 * provided by the effective implementation of {@link SessionIdTracker} has not yet expired on the server, 
	 * it is returned. Otherwise, if the session with this ID has expired, {@link ConnectionClosedException}
	 * is thrown.
	 * 
	 * Finally, if this connection is {@link Status#CLOSED_BY_CLIENT} or {@link Status#CLOSED_BY_SERVER}, 
	 * {@link ConnectionClosedException} is thrown.
	 * 
	 * @param sessionId The ID of the session you are looking to retrieve from the server.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}, if session exists, or {@code null} if no session with this ID
	 *         was found on the server. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same session ID will return the same object, so long as the session hasn't expired.
	 */
	Session getSessionById(String sessionId);

	/**
	 * Get the XDM schema, associated with this connection.
	 * Does not throw {@link ConnectionClosedException} if the connection is closed.
	 * 
	 * @return An object of type {@link Schema}
	 * 
	 * @since 0.7
	 */
	Schema getSchema();

	/**
	 * Most recent known {@link ConnectionStatus} of this connection.
	 * 
	 * @since 0.7
	 * @return An element of enum {@link ConnectionStatus}.
	 */
	ConnectionStatus getStatus();
	
	/**
	 * Close this connection. 
	 * All sessions opened in this connection are destroyed and will not be available 
	 * for retrieval from parallel connections. No-op if this connection has already been closed.
	 * 
	 * @since 0.7
	 */
	void close();

	/**
	 * Register a session expiration listener. If multiple listeners have been registered with a
	 * connection, they are posted in the order they were registered. 
	 * @param listener
	 */
	void registerExpirationListener(ExpirationListener listener);

	/**
	 * Interface to be implemented by a session expiration listener class, whose instance can
	 * be passed to {@link Connection#registerExpirationListener(ExpirationListener)}.
	 * 
	 * @since 0.8
	 */
	public interface ExpirationListener {
	   /**
	    * The callback function to be called by Variant Client right after the session,
	    * passed to this method, is expired.
	    * 
	    * @since 0.8
	    * @param session: The expired Variant session.
	    */
	   public void expired(Session session);
	}

}
