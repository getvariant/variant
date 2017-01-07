package com.variant.client.impl;

import com.variant.client.ClientException;
import com.variant.core.UserErrorException;

/**
 * We repeat the functionality of the core user exception as we want this class
 * to inherit from ClientException.
 * 
 * @author Igor
 */
public class ClientLocalErrorException extends ClientException {
	   
	private static final long serialVersionUID = 1L;

	public ClientLocalErrorException(ClientUserError error, Object...args) {
		super(error, args);
	}

	public ClientLocalErrorException(ClientUserError error, Throwable t, Object...args) {
		super(error, t, args);
	}

}
