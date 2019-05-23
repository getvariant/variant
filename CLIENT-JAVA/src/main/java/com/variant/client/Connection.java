package com.variant.client;

import java.util.Optional;



/**
 * Connection to a particular schema on a Variant server. 
 * The first operation a new Variant client instance must
 * do is connect to a particular schema on a Variant server by calling {@link VariantClient#connectTo(String)}.
 * Variant connections are stateless, i.e. no information about this connection is retained on the server.
 * New sessions are created against the live generation of the variation schema named in the connection. If no
 * such schema exists on the server, the {@link UnknownSchemaException} is thrown.
 * 
 * @since 0.7
 */
public interface Connection {
	
	/**
     * <p>The Variant client instance, which created this connection. 
     *  
	 * @return An instance of the {@link VariantClient} object, which originally created this connection
	 *         via the {@link VariantClient#connectTo(String)} call.
	 *
	 * @since 0.7
	 */
	VariantClient getClient();

	/**
	 * Get, if exists, or create, if does not exist, the Variant session with the externally tracked ID.
	 * If the session was not found on the server, a new session is created with a new session ID and
	 * the session tracker is updated accordingly. 
     *
	 * @param userData An array of zero or more opaque objects which is passed, without interpretation,
	 *                 to the constructor of the session ID tracker.

	 * @return An object of type {@link Session}. Cannot be <code>null</code>.
	 *
	 * @throws UnknownSchemaException
     *
	 * @since 0.7
     */
	Session getOrCreateSession(Object... userData);

	/**
	 * Get, if exists, the Variant session with the externally tracked ID. 
	 *  
	 * @param userData An array of zero or more opaque objects which is passed, without interpretation,
	 *                 to the constructor of the session ID tracker.
     *
     * @return An {@link Optional}, containing the {@link Session} object if the session exists on Variant server,
     *         or empty otherwise.
     * 
	 * @throws UnknownSchemaException
	 * 
	 * @since 0.7
	 */
	Optional<? extends Session> getSession(Object... userData);

	/**
	 * Get, if exists, the Variant session by its ID. Intended for downstream components of the host application,
	 * which do not implement an external session ID tracker, but rely on the upstream to create Variant session
	 * and pass down its ID.
	 * 
	 * @param sessionId The ID of the session you are looking to retrieve from the server.
     *
	 * @return An {@link Optional}, containing the {@link Session} object, if session with the given ID
	 *         exists on Variant server, or empty otherwise.
	 *         
	 * @throws UnknownSchemaException
     *
     * @since 0.7
	 */
	Optional<? extends Session> getSessionById(String sessionId);
	
	/**
	 * The name of the schema which is the target of this connection. Note that only schema name is retained at the connection level.
	 * Individual sessions created with this connection object, may be connected to different generations of this schema.
	 *
	 * @since 0.9
	 */
	String getSchemaName();

}
