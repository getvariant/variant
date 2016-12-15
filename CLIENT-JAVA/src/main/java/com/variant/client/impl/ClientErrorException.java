package com.variant.client.impl;

import com.variant.core.exception.RuntimeError;
import com.variant.core.exception.RuntimeErrorException;

/**
 * @author Igor
 */
public class ClientErrorException extends RuntimeErrorException {
	   
	private static final long serialVersionUID = 1L;

	public ClientErrorException(RuntimeError error, Object...args) {
		super(error, args);
	}

	public ClientErrorException(RuntimeError error, Throwable t, Object...args) {
		super(error, t, args);
	}

}
