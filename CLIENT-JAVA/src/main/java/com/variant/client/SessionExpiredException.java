package com.variant.client;


/**
 * Thrown when the underlying session has expired, but not the connection.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class SessionExpiredException extends ClientException.User {
	
	public SessionExpiredException() {
		super(ClientUserError.SESSION_EXPIRED);
	}

/*
	public SessionExpiredException(ClientException.User e) {
		super(ClientUserError.CONNECTION_CLOSED, e);
	}
*/
}