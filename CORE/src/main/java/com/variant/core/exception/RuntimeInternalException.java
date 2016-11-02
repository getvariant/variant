package com.variant.core.exception;

public class RuntimeInternalException extends VariantRuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param msg
	 */
	public RuntimeInternalException(String msg) {
		super(msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public RuntimeInternalException(Throwable t) {
		super(t);
	}

	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public RuntimeInternalException(String msg, Throwable t) {
		super(msg, t);
	}

}
