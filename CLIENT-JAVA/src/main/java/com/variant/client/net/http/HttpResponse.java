package com.variant.client.net.http;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

public class HttpResponse {

	public final HttpRequest request;
	public final int status;
	public final String body;
	public final Header[] headers;
	
	/**
	 * Fully consumed the response entity and retain what we need in this object,
	 * so that the underlying response can be closed by the caller.
	 * 
	 * @param entity
	 * @throws IOException 
	 * @throws ParseException 
	 */
	HttpResponse(HttpRequest request, CloseableHttpResponse underlyingResponse) throws ParseException, IOException {

		this.request = request;
		this.status = underlyingResponse.getStatusLine().getStatusCode();
		this.headers = underlyingResponse.getAllHeaders();
		HttpEntity entity = underlyingResponse.getEntity();
		this.body = entity == null ? null : EntityUtils.toString(entity);
	}
		
	@Override
	public String toString() {
		StringBuilder buff = new StringBuilder();
		buff.append(status).append(System.getProperty("line.separator"));
		for (Header h: headers) {
			buff.append(h).append(System.getProperty("line.separator"));
		}
		buff.append(body);
		return buff.toString();
	}
}
