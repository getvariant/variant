package com.variant.client;

import com.variant.core.error.UserError;

/**
 * Superclass of all user exceptions thrown by Variant Java Client
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class VariantException extends com.variant.core.error.VariantException {
	
	private UserError error = null;
	private Object[] args = null;
	
	/**
	 * Exposed for the use by client code.
	 * @param msg
	 */
	public VariantException(String msg) {
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
	public VariantException(Throwable t, UserError error, Object...args) {
		super.initCause(t);
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
