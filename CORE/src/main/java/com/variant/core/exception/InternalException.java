package com.variant.core.exception;

import com.variant.core.VariantException;

@SuppressWarnings("serial")
public class InternalException extends VariantException {

	private static final String MSG_PREFIX = "Internal exception: ";
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public InternalException(Throwable t) {
		super(MSG_PREFIX + t.getMessage(), t);
	}
	
	/**
	 * 
	 * @param msg
	 */
	public InternalException(String msg) {
		super(MSG_PREFIX + msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public InternalException(String msg, Throwable t) {
		super(MSG_PREFIX + msg, t);
	}
}
