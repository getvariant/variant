package com.variant.client;

import com.variant.core.impl.ServerError;


/**
 * Thrown when an operation is aborted because the underlying schema
 * does not exist.
 * 
 * @since 0.9
 */
@SuppressWarnings("serial")
public class UnknownSchemaException extends ClientException.User {
	
	public UnknownSchemaException(String schema) {
		super(ServerError.UnknownSchema, schema);
	}

}