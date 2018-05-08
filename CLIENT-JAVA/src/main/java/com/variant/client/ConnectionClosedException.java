package com.variant.client;

import com.variant.client.impl.ClientUserError;


/**
 * Thrown when the underlying connection has been closed.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ConnectionClosedException extends ClientException.User {
	
	public ConnectionClosedException(ClientUserError error) {
		super(error);
	}

}