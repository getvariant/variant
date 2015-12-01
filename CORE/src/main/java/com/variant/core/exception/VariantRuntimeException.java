package com.variant.core.exception;

import com.variant.core.schema.impl.MessageTemplate;
import com.variant.core.schema.parser.Severity;

public class VariantRuntimeException extends VariantException {

	private static final long serialVersionUID = 1L;
	private MessageTemplate template;
	private String[] args;
	
	public VariantRuntimeException(MessageTemplate template, String...args) {
		super();
		this.template = template;
		this.args = args;
	}

	public VariantRuntimeException(MessageTemplate template, Throwable t, String...args) {
		super(t);
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
