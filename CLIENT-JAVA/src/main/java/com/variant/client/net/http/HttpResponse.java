package com.variant.client.net.http;

import java.io.IOException;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.ClientException;
import com.variant.client.InternalErrorException;

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
	HttpResponse(HttpUriRequest request, CloseableHttpResponse underlyingResponse) throws ParseException, IOException {

		this.request = request;
		this.status = underlyingResponse.getStatusLine().getStatusCode();
		this.headers = underlyingResponse.getAllHeaders();
		HttpEntity entity = underlyingResponse.getEntity();
		this.body = entity == null ? null : EntityUtils.toString(entity);
	}
	
	/**
	 * Client exception contained herein.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	ClientException toClientException() {

		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> map = null;
				
		try {
			map = jacksonDataMapper.readValue(body, Map.class);
			Integer code = (Integer) map.get("code");
			String message = (String) map.get("message");
			String comment = (String) map.get("comment");
			
			switch (code) {
			case 601: return new InternalErrorException(message, comment);
			//case 701: return new Unknown
			
			default: return new ClientException(code, message, comment);

			}
		}
		catch(IOException parseException) {
			return new ClientException(0, body);
		}
	
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
