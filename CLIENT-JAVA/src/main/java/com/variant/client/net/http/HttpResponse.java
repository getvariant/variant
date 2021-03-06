package com.variant.client.net.http;

import java.io.IOException;
import java.util.List;
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
import com.variant.client.SessionExpiredException;
import com.variant.client.VariantException;
import com.variant.client.impl.ClientInternalError;
import com.variant.core.error.ServerError;

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
	HttpResponse(HttpUriRequest request, CloseableHttpResponse response) throws ParseException, IOException {

		this.request = request;
		this.status = response.getStatusLine().getStatusCode();
		this.headers = response.getAllHeaders();
		HttpEntity entity = response.getEntity();
		this.body = entity == null ? null : EntityUtils.toString(entity);
	}
	
	/**
	 * The Variant exception contained herein.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	VariantException toVariantException() {

		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> map = null;
				
		try {
			// Reconstitute the server error.
			map = jacksonDataMapper.readValue(body, Map.class);
			Integer code = (Integer) map.get("code");
			List<String> args = (List<String>) map.get("args");
			ServerError error = ServerError.byCode(code);
			
			if (error.isInternal()) {
				return VariantException.internal(ClientInternalError.INTERNAL_SERVER_ERROR, error.asMessage(args.toArray()));
			}
			else {	   
			  return new VariantException(error, args.toArray(new String[args.size()]));
			}
		}
		catch(IOException parseException) {
			return VariantException.internal(ClientInternalError.INTERNAL_SERVER_ERROR, parseException.getMessage());
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
