package com.variant.core.exception;

import com.variant.core.schema.parser.MessageTemplate;
import com.variant.core.schema.parser.Severity;

public class VariantBootstrapException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private MessageTemplate template;
	private String[] args;
	
	public VariantBootstrapException(MessageTemplate template, String...args) {
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
