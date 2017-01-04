package com.variant.client;

/**
 * Exceptions that are not due to user action.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class InternalErrorException extends ClientException {
	
	public InternalErrorException(String message, String comment) {
		super(601, message, comment);
	}

	public InternalErrorException(String message) {
		super(601, message);
	}

	public InternalErrorException(String message, String comment, Throwable t) {
		super(601, message, comment, t);
	}

	public InternalErrorException(String message, Throwable t) {
		super(601, message, t);
	}

}
