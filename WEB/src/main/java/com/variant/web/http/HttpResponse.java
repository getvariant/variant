package com.variant.web.http;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;

public class HttpResponse {

	StatusLine status = null;
	String body = null;
	Header[] headers = null;
	
	/**
	 * Fully consumed the response entity and retain what we need in this object,
	 * so that the underlying response can be closed by the caller.
	 * 
	 * @param entity
	 * @throws IOException 
	 * @throws ParseException 
	 */
	HttpResponse(CloseableHttpResponse underlyingResponse) throws ParseException, IOException {
		status = underlyingResponse.getStatusLine();
		body = EntityUtils.toString(underlyingResponse.getEntity());
		headers = underlyingResponse.getAllHeaders();
	}
	
	/**
	 * Status code of the response
	 * @return
	 */
	public int getStatus() {
		return status.getStatusCode();
	}
	
	public String getBody() {
		return body;
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
