package com.variant.client.servlet;

import com.variant.client.ClientException;

@SuppressWarnings("serial")
public class ServletClientException extends ClientException.Internal {

	public ServletClientException(String msg) {
		super(msg);
	}
}
