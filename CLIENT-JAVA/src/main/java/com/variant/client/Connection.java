package com.variant.client;

import com.variant.core.schema.Schema;


/**
 * A connection to the server.
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
	 * of {@link VariantSessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned. Otherwise, a new session is created. If the session has not expired but the 
	 * schema has changed since it was created, this call will throw an unchecked 
	 * {@link VariantSchemaModifiedException}.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link VariantSessionIdTracker#init(VariantInitParams, Object...)}
	 *                 and {@link VariantTargetingTracker#init(VariantInitParams, Object...)}.
     *
	 * @since 0.7
	 * @return An object of type {@link VariantSession}. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same arguments will return the same object, unless the session expired between the
	 *         invocations, in which case a new object will be returned. Never returns <code>null</code>.
	 */
	VariantSession getOrCreateSession(Object... userData);

	/**
	 * Get caller's current Variant session. If the session ID exists in the underlying implementation 
	 * of {@link VariantSessionIdTracker} and the session with this session ID has not expired on the server,
	 * this session is returned.  If the session has not expired but the schema has changed since it was created, 
	 * this call will throw an unchecked {@link VariantSchemaModifiedException}.
	 * 
	 * 
	 * @param userData An array of zero or more opaque objects which will be passed without interpretation
	 *                 to the implementations of {@link VariantSessionIdTracker#init(Object...)}
	 *                 and {@link VariantTargetingTracker#init(VariantInitParams, Object...)}.
     *
	 * @since 0.7
	 * @return An object of type {@link VariantSession}. This call is guaranteed to be idempotent, i.e. a subsequent
	 *         invocation with the same arguments will return the same object or <code>null</code>.
	 */
	VariantSession getSession(Object... userData);

	/**
	 * <p>Get the XDM schema associated with this connection.
	 * 
	 * @return An object of type {@link Schema}
	 * 
	 * @since 0.7
	 */
	public Schema getSchemama();

	/**
	 * The status of this connection.
	 * 
	 * @since 0.7
	 * @return An object of type {@link Status}..
	 */
	Status getStatus();
	
	/**
	 * Close this connection.
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
