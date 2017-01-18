package com.variant.client;


/**
 * Thrown when the underlying connection has been closed.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ConnectionClosedException extends ClientException.User {
	
	public ConnectionClosedException() {
		super(ClientUserError.CONNECTION_CLOSED);
	}

	public ConnectionClosedException(ClientException.User e) {
		super(ClientUserError.CONNECTION_CLOSED, e);
	}

}