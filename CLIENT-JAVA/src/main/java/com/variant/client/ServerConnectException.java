package com.variant.client;

/**
 * Thrown when client is unable to obtain a network connection to Variant server.
 * 
 * @since 0.9
 */
@SuppressWarnings("serial")
public class ServerConnectException extends VariantException {
	
	public ServerConnectException(String url) {
		super(VariantError.SERVER_CONNECTION_TIMEOUT, url);
	}

}
