package com.variant.core;

public class VariantInternalException extends RuntimeException {

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
	public VariantInternalException(String msg, Throwable t) {
		super(msg, t);
	}

}
