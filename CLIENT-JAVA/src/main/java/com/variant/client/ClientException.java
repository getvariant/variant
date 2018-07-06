package com.variant.client;

import com.variant.core.UserError;
import com.variant.core.impl.VariantException;

/**
 * Superclass of all user exceptions thrown by Variant Java Client
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class ClientException extends VariantException {
	
	private UserError error = null;
	private Object[] args = null;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	public ClientException(UserError error, Object...args) {
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
	public ClientException(UserError error, Throwable t, Object...args) {
		super(t);
		this.error = error;
		this.args = args;
	}

	/**
	 * 
	 * @param msg
	 *
	public User(String msg) {
		super(msg);
	}
*/
	
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
