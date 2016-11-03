package com.variant.core.exception;

public class RuntimeInternalException extends VariantRuntimeException {

	private static final long serialVersionUID = 1L;
	private static final String MSG_PREFIX = "Internal exception: ";
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public RuntimeInternalException(Throwable t) {
		super(MSG_PREFIX + t.getMessage(), t);
	}
	
	/**
	 * 
	 * @param msg
	 */
	public RuntimeInternalException(String msg) {
		super(MSG_PREFIX + msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public RuntimeInternalException(String msg, Throwable t) {
		super(MSG_PREFIX + msg, t);
	}
}
