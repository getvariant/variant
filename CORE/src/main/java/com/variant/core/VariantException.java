package com.variant.core;

/**
 * Top-level Variant exception. All variant exceptions inherit from here.
 * 
 * @author Igor Urisman
 * @since 0.5
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
	 */
	public VariantException(Throwable t) {
		super(t);
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
