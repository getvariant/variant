package com.variant.client.http;

import com.variant.core.exception.RuntimeInternalException;

public class VariantHttpClientException extends RuntimeInternalException {

	private static final long serialVersionUID = 1L;

	public VariantHttpClientException(HttpResponse resp) {
		super(makeMessage(resp));
	}
	
	/**
	 * @param resp
	 * @return
	 */
	private static String makeMessage(HttpResponse resp) {
		StringBuilder result = new StringBuilder();
		result.append("HTTP request  [").
			append(resp.getOriginalRequest().getRequestLine()).
			append("] failed: ").
			append(resp.getStatus()).
			append(" ").
			append(resp.body);
		return result.toString();
	}

}
