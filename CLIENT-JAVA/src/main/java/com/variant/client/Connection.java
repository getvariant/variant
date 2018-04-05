package com.variant.client;

import com.typesafe.config.Config;
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
	 * Get or create caller's current Variant session. If the session ID exists in the underlying implementation 
	 * of {@link SessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned. Otherwise, a new session is created.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed, without interpretation,
	 *                 to the implementations of {@link SessionIdTracker#init(Connection, Object...)}
	 *                 and {@link TargetingTracker#init(Connection, Object...)}.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}. This method is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same arguments will return the same object, unless the session expired between the
	 *         invocations, in which case a new object will be returned. Never returns <code>null</code>.
	 */
	Session getOrCreateSession(Object... userData);

	/**
	 * Get caller's current Variant session. If the session ID exists in the underlying implementation 
	 * of {@link SessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link SessionIdTracker#init(Connection, Object...)}
	 *                 and {@link TargetingTracker#init(Connection, Object...)}.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}, if the session represented by <code>userData</code> exists,
	 *         or <code>null</code> otherwise.
	 *         This method is idempotent, i.e. a subsequent
	 *         invocation with the same arguments will return the same object or <code>null</code>.
	 */
	Session getSession(Object... userData);

	/**
	 * Get caller's current Variant session by session ID.
	 * 
	 * 
	 * @param sessionId The session ID of the session we're trying to retrieve.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}, if session exists, or {@code null} if no session with this ID
	 *         was found on the server. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same session ID will return the same object, so long as the session hasn't expired.
	 */
	Session getSessionById(String sessionId);

	/**
	 * <p>Get the XDM schema, associated with this connection.
	 * 
	 * @return An object of type {@link Schema}
	 * 
	 * @since 0.7
	 */
	Schema getSchema();

	/**
	 * The status of this connection.
	 * 
	 * @since 0.7
	 * @return An object of type {@link Status}..
	 */
	Status getStatus();
	
	/**
	 * Externally supplied configuration.
	 * See https://github.com/typesafehub/config for details on Typesafe Config.
	 * A shortcut for {@code getClient().getConfig()}
	 * @return An instance of the {@link Config} type.
	 * 
	 * @since 0.7
	 */
	Config getConfig();

	/**
	 * Close this connection. No-op if this connection has already been closed.
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

	/**
	 * Status of a Variant {@link Connection}.
	 * 
	 * @since 0.7
	 */
	public enum Status {

		/**
		 * Internal state. Should never be returned by {@link Connection#getStatus()}.
		 * 
		 * @since 0.8
		 */
		CONNECTING, 

		/**
		 * Open and usable.
		 * 
		 * @since 0.7
		 */
		OPEN, 
		
		/**
		 * Connection has been closed by the client with a call to {@link Connection#close()()}
		 * 
		 * @since 0.7
		 */
		CLOSED_BY_CLIENT,
		
		/**
		 * Connection has been closed by the server as the result of a schema reload or server restart.
		 * 
		 * @since 0.7
		 */
		CLOSED_BY_SERVER
	}
}
