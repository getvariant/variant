package com.variant.core;

/**
 * The super-type for all Variant exception. 
 * All Variant exceptions, server and client, inherit from here.
 * 
 * @author Igor Urisman
 * @since 0.5
 *
 */
@SuppressWarnings("serial")
public class VariantException extends RuntimeException {

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
