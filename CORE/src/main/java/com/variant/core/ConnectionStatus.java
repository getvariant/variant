package com.variant.core;

/**
 * Status of a Variant connection, as returned by <code>Connection#getStatus()</code>
 * 
 * @since 0.7
 */
public enum ConnectionStatus {

	/**
	 * Open without restrictions. New sessions can be created.
	 * 
	 * @since 0.7
	 */
	OPEN, 
	
	/**
	 * Connection has been closed by the client with a call to {@link Connection#close()}.
	 * An attempt to use this connection will result in a <code>ConnectionClosedException</code>.
	 * 
	 * @since 0.7
	 */
	CLOSED_BY_CLIENT,
	
	/**
	 * The schema connected to by this connection has been undeployed by the server. Existing sessions
	 * will continue to be serviced over this connection until they expire, but no new sessions can be created. 
	 * An attempt to create a new session over a draining connection will result in a <code>ConnectionClosedException</code>.
	 * 
	 * @since 0.8
	 */
	DRAINING,

	/**
	 * Connection has been closed by the server because the schema connected to by this connection has been
	 * disposed of, or because of a server restart. No sessions can be retrieved or created over this connection.
	 * 
	 * @since 0.7
	 */
	CLOSED_BY_SERVER
}
