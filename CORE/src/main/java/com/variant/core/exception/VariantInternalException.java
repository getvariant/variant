package com.variant.core.exception;

public class VariantInternalException extends VariantException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param msg
	 */
	public VariantInternalException(String msg) {
		super(msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public VariantInternalException(Throwable t) {
		super(t);
	}

	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public VariantInternalException(String msg, Throwable t) {
		super(msg, t);
	}

}
