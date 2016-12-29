package com.variant.client.impl;

import com.variant.core.UserErrorException;
import com.variant.core.exception.CommonError;

/**
 * @author Igor
 */
public class ClientErrorException extends UserErrorException {
	   
	private static final long serialVersionUID = 1L;

	public ClientErrorException(CommonError error, Object...args) {
		super(error, args);
	}

	public ClientErrorException(CommonError error, Throwable t, Object...args) {
		super(error, t, args);
	}

}
