package com.variant.core.exception;

/**
 * All variant exception inherit from here.
 * 
 * @author Igor
 *
 */
public class VariantException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public VariantException() {
		super();
	}
	
	/**
	 * 
	 * @param msg
	 */
	public VariantException(String msg) {
		super(msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public VariantException(String msg, Throwable t) {
		super(msg, t);
	}


}
