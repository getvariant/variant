package com.variant.core;

import com.variant.core.error.ErrorTemplate;
import com.variant.core.error.Severity;

public class VariantBootstrapException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private ErrorTemplate template;
	private String[] args;
	
	public VariantBootstrapException(ErrorTemplate template, String...args) {
		this.template = template;
		this.args = args;
	}
	
	/**
	 * 
	 * @return
	 */
	public Severity getSeverity() {
		return template.getSeverity();
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String getMessage() {
		return String.format(template.getFormat(), (Object[]) args);
	}
}
