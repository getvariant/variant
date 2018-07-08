package com.variant.client;

import com.variant.core.UserError;

/**
 * Superclass of all user exceptions thrown by Variant Java Client
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class VariantException extends com.variant.core.impl.VariantException {
	
	private UserError error = null;
	private Object[] args = null;
	
	/**
	 * If client code wishes to extend this exception without bothering with also
	 * extending UserError.  Not used in JavaClient itself.
	 * 
	 * @param msg
	 */
	protected VariantException(String msg) {
		super(msg);
	}

	/**
	 * 
	 * @param template
	 * @param args
	 */
	public VariantException(UserError error, Object...args) {
		super();
		this.error = error;
		this.args = args;
	}

	/**
	 * 
	 * @param template
	 * @param t
	 * @param args
	 */
	public VariantException(UserError error, Throwable t, Object...args) {
		super(t);
		this.error = error;
		this.args = args;
	}
	
	/**
	 * 
	 * @return
	 */
	public UserError getError() {
		return error;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String getMessage() {
		return error == null ? super.getMessage() : error.asMessage(args);
	}
}
