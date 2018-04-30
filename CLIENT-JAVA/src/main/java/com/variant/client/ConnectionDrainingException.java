package com.variant.client;

import com.variant.client.impl.ClientUserError;


/**
 * Thrown when user attempts to create a new session over a draining connection.
 * 
 * @author Igor Urisman
 * @since 0.8
 */
@SuppressWarnings("serial")
public class ConnectionDrainingException extends ClientException.User {
	
	public ConnectionDrainingException() {
		super(ClientUserError.CONNECTION_DRAINING);
	}

}