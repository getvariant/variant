package com.variant.client;

import com.variant.share.error.ServerError;


/**
 * Thrown when the underlying Variant session has been expired on Variant server.
 * 
 * @since 0.7
 */
@SuppressWarnings("serial")
public class SessionExpiredException extends VariantException {
	
	public SessionExpiredException(String sid) {
		super(ServerError.SESSION_EXPIRED, sid);
	}

}