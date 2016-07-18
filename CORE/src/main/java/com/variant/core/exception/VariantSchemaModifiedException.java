package com.variant.core.exception;

import com.variant.core.schema.impl.MessageTemplate;

public class VariantSchemaModifiedException extends VariantExpectedRuntimeException {

	private static final long serialVersionUID = 1L;
	
	public VariantSchemaModifiedException(String currentSchemaId, String originalSchemaId) {
		super(MessageTemplate.RUN_SCHEMA_MODIFIED, currentSchemaId, originalSchemaId);
	}
	
}
