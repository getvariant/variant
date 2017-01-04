package com.variant.client.impl;

import com.variant.core.UserErrorException;

/**
 * @author Igor
 */
public class ClientUserErrorException extends UserErrorException {
	   
	private static final long serialVersionUID = 1L;

	public ClientUserErrorException(ClientUserError error, Object...args) {
		super(error, args);
	}

	public ClientUserErrorException(ClientUserError error, Throwable t, Object...args) {
		super(error, t, args);
	}

}
