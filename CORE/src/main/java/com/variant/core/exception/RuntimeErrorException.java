package com.variant.core.exception;

import com.variant.core.exception.Error.Severity;

public class RuntimeErrorException extends VariantRuntimeException {

	private static final long serialVersionUID = 1L;
	private RuntimeError error;
	private Object[] args;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	public RuntimeErrorException(RuntimeError error, Object...args) {
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
	public RuntimeErrorException(RuntimeError error, Throwable t, Object...args) {
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
