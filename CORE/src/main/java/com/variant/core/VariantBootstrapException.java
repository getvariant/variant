package com.variant.core;

/**
 * 
 * @author Igor
 *
 */
public class VariantBootstrapException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param msg
	 */
	public VariantBootstrapException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param msg
	 * @param t
	 */
	public VariantBootstrapException(String msg, Throwable t) {
		super(msg, t);
	}

}
