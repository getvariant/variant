package com.variant.client;

/**
 * Thrown when the underlying connection has been closed.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class InternalErrorException extends ClientException {
	
	public InternalErrorException(String message, String comment) {
		super(601, message, comment);
	}

}
