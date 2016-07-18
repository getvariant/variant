package com.variant.core.exception;

import com.variant.core.schema.impl.MessageTemplate;

public class VariantBootstrapException extends VariantExpectedRuntimeException {

	private static final long serialVersionUID = 1L;
	
	public VariantBootstrapException(MessageTemplate template, Object...args) {
		super(template, args);
	}
	
}
