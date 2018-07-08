package com.variant.client;

import com.variant.client.impl.ClientUserError;


/**
 * Thrown when client is unable to obtain a network connection to Variant server.
 * 
 * @since 0.9
 */
@SuppressWarnings("serial")
public class ServerConnectException extends VariantException {
	
	public ServerConnectException(String url) {
		super(ClientUserError.SERVER_CONNECTION_TIMEOUT, url);
	}

}
