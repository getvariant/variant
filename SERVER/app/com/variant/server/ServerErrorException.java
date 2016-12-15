package com.variant.server;

import com.variant.core.exception.RuntimeError;
import com.variant.core.exception.RuntimeErrorException;

public class ServerErrorException extends RuntimeErrorException {
	   
	private static final long serialVersionUID = 1L;

	public ServerErrorException(RuntimeError error, Object...args) {
		super(error, args);
	}

	public ServerErrorException(RuntimeError error, Throwable t, Object...args) {
		super(error, t, args);
	}

}