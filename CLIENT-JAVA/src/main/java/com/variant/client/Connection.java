package com.variant.client;

import java.util.Optional;



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
	 * Get, if exists, or create, if does not exist, the Variant session with the externally tracked ID.
	 * If the session was not found on the server, a new session is created with a new session ID and
	 * the session tracker is updated accordingly. 
     *
	 * @param userData An array of zero or more opaque objects which will be passed, without interpretation,
	 *                 to the implementations of {@link SessionIdTracker#init(Object...)}
	 *                 and {@link TargetingTracker#init(Session, Object...)}.

	 * @return An object of type {@link Session}. Never returns <code>null</code>.
	 *
	 * @throws UnknownSchemaException
     *
	 * @since 0.7
     */
	Session getOrCreateSession(Object... userData);

	/**
	 * Get, if exists, the Variant session with the externally tracked ID. 
	 *  
	 * @param userData An array of zero or more opaque objects which will be passed, without interpretation,
	 *                 to the implementations of {@link SessionIdTracker#init(Object...)}
	 *                 and {@link TargetingTracker#init(Session, Object...)}.
     *
     * @return An {@link Optional}, containing the {@link Session} object if the session exists on Variant server,
     *         or empty otherwise.
     * 
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	Optional<Session> getSession(Object... userData);

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
	 * {@link UnknownSchemaException} is thrown.
	 * 
	 * @param sessionId The ID of the session you are looking to retrieve from the server.
     *
	 * @return An object of type {@link Session}, if session exists, or {@code null} if no session with this ID
	 *         was found on the server. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same session ID will return the same object, so long as the session hasn't expired.
	 *         
	 * @throws UnknownSchemaException
     *
     * @since 0.7
	 */
	Session getSessionById(String sessionId);
	
	/**
	 * The name of the schema which is the target of this connection.
	 *
	 * @since 0.9
	 */
	String getSchemaName();

}
