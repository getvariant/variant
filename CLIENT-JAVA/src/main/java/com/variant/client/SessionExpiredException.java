package com.variant.client;

import com.variant.client.impl.ClientUserError;


/**
 * Thrown when the underlying Variant session has expired, but the connection which opened this session is still usable.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class SessionExpiredException extends ClientException.User {
	
	public SessionExpiredException() {
		super(ClientUserError.SESSION_EXPIRED);
	}

	public SessionExpiredException(ClientException.User e) {
		super(ClientUserError.SESSION_EXPIRED, e);
	}

}