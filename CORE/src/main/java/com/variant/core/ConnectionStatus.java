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
	 * Connection has been closed by the client with a call to {@link Connection#close()()}.
	 * No sessions can be retrieved or created.
	 * 
	 * @since 0.7
	 */
	CLOSED_BY_CLIENT,
	
	/**
	 * The schema connected to by this connection has been undeployed by the server. Existing sessions
	 * will continue to be serviced over this connection. But no new sessions created. An attempt to
	 * create a new session over a draining connection will result in <code>ConnectionDrainingException</code>.
	 * 
	 * @since 0.8
	 */
	DRAINING,

	/**
	 * Connection has been closed by the server as the result of a schema reload or server restart.
	 * No sessions can be retrieved or created over this connection.
	 * 
	 * @since 0.7
	 */
	CLOSED_BY_SERVER
}
