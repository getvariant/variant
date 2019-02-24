package com.variant.client;

import com.variant.core.error.ServerError;


/**
 * Thrown when the underlying Variant session has expired.
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class SessionExpiredException extends VariantException {
	
	public SessionExpiredException(String sid) {
		super(ServerError.SESSION_EXPIRED, sid);
	}

}