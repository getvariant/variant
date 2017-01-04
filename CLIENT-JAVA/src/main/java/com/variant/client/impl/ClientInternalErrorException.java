package com.variant.client.impl;

import com.variant.core.UserErrorException;

/**
 * @author Igor
 */
public class ClientInternalErrorException extends UserErrorException {
	   
	private static final long serialVersionUID = 1L;

	public ClientInternalErrorException(ClientInternalError error, Object...args) {
		super(error, args);
	}

	public ClientInternalErrorException(ClientInternalError error, Throwable t, Object...args) {
		super(error, t, args);
	}

}
