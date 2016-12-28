package com.variant.core;

import com.variant.core.UserError.Severity;
import com.variant.core.exception.CommonError;

public class UserException extends VariantException {

	private static final long serialVersionUID = 1L;
	private CommonError error;
	private Object[] args;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	public UserException(CommonError error, Object...args) {
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
	public UserException(CommonError error, Throwable t, Object...args) {
		super(t);
		this.error = error;
		this.args = args;
	}

	/**
	 * 
	 * @return
	 */
	public Severity getSeverity() {
		return error.severity;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String getMessage() {
		return String.format(error.format, (Object[]) args);
	}

}
