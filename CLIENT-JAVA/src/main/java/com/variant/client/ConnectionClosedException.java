package com.variant.client;

/**
 * Thrown when the underlying connection has been closed.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ConnectionClosedException extends ClientException {
	
	public ConnectionClosedException(String message, String comment) {
		super(701, message, comment);
	}

}
