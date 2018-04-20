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
	 * Connection has been closed by the server as the result of a schema reload or server restart.
	 * No sessions can be retrieved or created.
	 * 
	 * @since 0.7
	 */
	CLOSED_BY_SERVER
}
