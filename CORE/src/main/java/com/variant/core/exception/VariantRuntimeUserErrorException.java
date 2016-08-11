package com.variant.core.exception;

import com.variant.core.xdm.impl.MessageTemplate;

public class VariantRuntimeUserErrorException extends VariantExpectedRuntimeException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public VariantRuntimeUserErrorException(MessageTemplate template, Object...args) {
		super(template, args);
	}

	/**
	 * 
	 */
	public VariantRuntimeUserErrorException(MessageTemplate template, Throwable t, Object...args) {
		super(template, t, args);
	}

}
