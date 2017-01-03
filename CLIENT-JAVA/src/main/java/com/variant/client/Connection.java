package com.variant.client;

import com.variant.core.schema.Schema;


/**
 * A connection to the server. 
 * Not thread safe, avoid sharing a connection object between threads.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
public interface Connection {
	
	/**
     * <p>The Variant client instance that created this connection. 
     *  
	 * @return An instance of the {@link VariantClient} object, which originally created this object
	 *         via {@link VariantClient#getSession(Object...)}.
	 *
	 * @since 0.7
	 */
	VariantClient getClient();

	/**
	 * Get or create caller's current Variant session. If the session ID exists in the underlying implementation 
	 * of {@link SessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned. Otherwise, a new session is created. If the session has not expired but the 
	 * schema has changed since it was created, this call will throw an unchecked 
	 * {@link VariantSchemaModifiedException}.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link SessionIdTracker#init(VariantInitParams, Object...)}
	 *                 and {@link TargetingTracker#init(VariantInitParams, Object...)}.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same arguments will return the same object, unless the session expired between the
	 *         invocations, in which case a new object will be returned. Never returns <code>null</code>.
	 */
	Session getOrCreateSession(Object... userData);

	/**
	 * Get caller's current Variant session by . If the session ID exists in the underlying implementation 
	 * of {@link SessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link SessionIdTracker#init(Object...)} in order to obtain
	 *                 the session ID of the session we're trying to retrieve.
     *
	 * @since 0.7
	 * @return An object of type {@link Session}. This call is guaranteed to be idempotent, i.e. a subsequent
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
	 * <p>Get the XDM schema associated with this connection.
	 * 
	 * @return An object of type {@link Schema}
	 * 
	 * @since 0.7
	 */
	public Schema getSchema();

	/**
	 * The status of this connection.
	 * 
	 * @since 0.7
	 * @return An object of type {@link Status}..
	 */
	Status getStatus();
	
	/**
	 * Close this connection. No-op if this connection has already been closed.
	 * 
	 * @since 0.7
	 */
	void close();
	
	/**
	 * Possible status of a Variant connection.
	 * 
	 * @since 0.7
	 */
	public enum Status {

		/**
		 * Open and usable.
		 * 
		 * @since 0.7
		 */
		OPEN, 
		
		/**
		 * Connection has been closed by the client with a call to {@link Connection#close()}
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
