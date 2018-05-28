package com.variant.client;

import com.variant.client.lifecycle.LifecycleEvent;
import com.variant.client.lifecycle.LifecycleHook;
import com.variant.core.ConnectionStatus;
import com.variant.core.schema.Schema;


/**
 * Represents a connection to a particular schema on a Variant server. 
 * The first operation a new Variant client instance must
 * do is connect to a particular schema on a Variant server by calling {@link VariantClient#getConnection(String)}
 * 
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
     * <p>This connection's unique immutable ID, as was assigned by the server at the time this
     * connection was open. 
     *  
	 * @since 0.7
	 */
	String getId();

	/**
	 * Get the Variant schema, associated with this connection.
	 * 
	 * @return An object of type {@link Schema}
	 * 
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.7
	 */
	Schema getSchema();

	/**
	 * Get, if exists, or create, if does not exist, the Variant session with the externally tracked ID.
	 * 
	 * Under normal circumstances, when this connection is {@link ConnectionStatus#OPEN}, the following behavior
	 * is expected. If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned. 
	 * Otherwise, a new session with this ID is created.
	 * 
	 * This method is idempotent, i.e. a subsequent calls with the same parameters
	 * will return the same object, unless the session has expired between the calls,
	 * in which case a brand new object will be returned.
	 * 
	 * However, if this connection is {@link ConnectionConnectionStatus#DRAINING}, no new sessions can be created. Therefore, 
	 * If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned, but if the
	 * session with this ID has expired, {@link ConnectionDrainingException} is thrown.
	 * 
	 * Finally, if this connection is {@link ConnectionStatus#CLOSED_BY_CLIENT} or {@link ConnectionStatus#CLOSED_BY_SERVER}, 
	 * {@link ConnectionClosedException} is thrown.
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed, without interpretation,
	 *                 to the implementations of {@link SessionIdTracker#init(Connection, Object...)}
	 *                 and {@link TargetingTracker#init(Connection, Object...)}.
     *
	 * @return An object of type {@link Session}. Never returns <code>null</code>.
	 *
	 * @throws ConnectionClosedException
     *
	 * @since 0.7
     */
	Session getOrCreateSession(Object... userData);

	/**
	 * Get, if exists, the Variant session with the externally tracked ID.
	 * 
	 * Under normal circumstances, when this connection is {@link ConnectionStatus#OPEN}, the following behavior
	 * is expected. If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned. 
	 * Otherwise, this method returns <code>null</code>.
	 * 
	 * This method is idempotent, i.e. a subsequent calls with the same parameters
	 * will return the same object, unless the session has expired between the calls,
	 * in which case a brand new object will be returned.
	 * 
	 * However, if this connection is {@link ConnectionStatus#DRAINING}, and the session with the ID 
	 * provided by the effective implementation of {@link SessionIdTracker} has not yet expired on the server, 
	 * it is returned. Otherwise, if the session with this ID has expired, {@link ConnectionClosedException}
	 * is thrown.
	 * 
	 * Finally, if this connection is {@link ConnectionStatus#CLOSED_BY_CLIENT} or {@link ConnectionStatus#CLOSED_BY_SERVER}, 
	 * {@link ConnectionClosedException} is thrown.
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link SessionIdTracker#init(Connection, Object...)}
	 *                 and {@link TargetingTracker#init(Connection, Object...)}.
     *
	 * @return An object of type {@link Session}, if the session exists, or <code>null</code> otherwise.
     *
	 * @throws ConnectionClosedException
	 * 
	 * @since 0.7
	 */
	Session getSession(Object... userData);

	/**
	 * Get, if exists, the Variant session with the externally tracked ID.
	 * 
	 * Under normal circumstances, when this connection is {@link ConnectionStatus#OPEN}, the following behavior
	 * is expected. If the session with the ID provided by the effective implementation 
	 * of {@link SessionIdTracker} has not yet expired on the server, it is returned. 
	 * Otherwise, this method returns <code>null</code>.
	 * 
	 * This method is idempotent, i.e. a subsequent calls with the same parameters
	 * will return the same object, unless the session has expired between the calls,
	 * in which case a brand new object will be returned.
	 * 
	 * However, if this connection is {@link ConnectionStatus#DRAINING}, and the session with the ID 
	 * provided by the effective implementation of {@link SessionIdTracker} has not yet expired on the server, 
	 * it is returned. Otherwise, if the session with this ID has expired, {@link ConnectionDrainingException}
	 * is thrown.
	 * 
	 * Finally, if this connection is {@link ConnectionStatus#CLOSED_BY_CLIENT} or {@link ConnectionStatus#CLOSED_BY_SERVER}, 
	 * {@link ConnectionClosedException} is thrown.
	 * 
	 * @param sessionId The ID of the session you are looking to retrieve from the server.
     *
	 * @return An object of type {@link Session}, if session exists, or {@code null} if no session with this ID
	 *         was found on the server. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same session ID will return the same object, so long as the session hasn't expired.
	 *         
	 * @throws ConnectionClosedException
     *
     * @since 0.7
	 */
	Session getSessionById(String sessionId);

	/**
	 * Most recent known {@link ConnectionStatus} of this connection.
	 * 
	 * @return An element of enum {@link ConnectionStatus}.
	 * @since 0.7
	 */
	ConnectionStatus getStatus();
	
	/**
	 * Close this connection. All local session objects created by this connection will become inaccessible.
	 * However, the closure of the connection has no effect on the distributed session that had been
	 * accessed by or created by this connection: these will remain active and accessible via the {@link #getSessionById(String)}
	 * method until they expire naturally. 
	 * 
	 * @throws ConnectionClosedException
     *
	 * @since 0.7
	 */
	void close();

	/**
	 * Register a connection-level life-cycle hook. Hooks are posted asynchronously.
	 * If multiple hooks are registered for a particular life-cycle event, connection-specific
	 * hooks are posted first, followed by connection-specific hooks.  
	 * order is undefined.
	 * 
	 * @param An implementation of {@link Lifecycle}
	 * @since 0.8
	 */
	void addLifecycleHook(LifecycleHook<? extends LifecycleEvent> hook);

}
