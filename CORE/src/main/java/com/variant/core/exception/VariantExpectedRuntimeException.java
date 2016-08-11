package com.variant.core.exception;

import com.variant.core.schema.ParserMessage.Severity;
import com.variant.core.xdm.impl.MessageTemplate;

public class VariantExpectedRuntimeException extends VariantRuntimeException {

	private static final long serialVersionUID = 1L;
	private MessageTemplate template;
	private Object[] args;
	
	/**
	 * 
	 * @param template
	 * @param args
	 */
	public VariantExpectedRuntimeException(MessageTemplate template, Object...args) {
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
	public VariantExpectedRuntimeException(MessageTemplate template, Throwable t, Object...args) {
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
