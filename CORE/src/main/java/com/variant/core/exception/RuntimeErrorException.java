package com.variant.core.exception;

import com.variant.core.exception.Error.Severity;

public class RuntimeErrorException extends VariantRuntimeException {

	private static final long serialVersionUID = 1L;
	private RuntimeError template;
	private Object[] args;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	public RuntimeErrorException(RuntimeError template, Object...args) {
		super();
		this.template = template;
		this.args = args;
	}

	/**
	 * 
	 * @param template
	 * @param t
	 * @param args
	 */
	public RuntimeErrorException(RuntimeError template, Throwable t, Object...args) {
		super(t);
		this.template = template;
		this.args = args;
	}

	/**
	 * 
	 * @return
	 */
	public Severity getSeverity() {
		return template.severity;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String getMessage() {
		return String.format(template.format, (Object[]) args);
	}

}
