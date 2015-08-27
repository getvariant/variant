package com.variant.core.exception;

/**
 * All variant exception inherit from here.
 * 
 * @author Igor
 *
 */
abstract public class VariantException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	protected VariantException() {
		super();
	}
	
	/**
	 * 
	 * @param msg
	 */
	protected VariantException(String msg) {
		super(msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @param t
	 */
	protected VariantException(String msg, Throwable t) {
		super(msg, t);
	}


}
