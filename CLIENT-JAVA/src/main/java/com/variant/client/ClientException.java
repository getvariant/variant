package com.variant.client;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.net.http.HttpResponse;


public class ClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public final int code;
	public final String message;
	public final String comment;
	
	/**
	 * 
	 * @param httpResponse
	 */
	@SuppressWarnings("unchecked")
	public ClientException(HttpResponse resp) {

		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> map = null;
		
		int _code;
		String _message, _comment;
		
		try {
			map = jacksonDataMapper.readValue(resp.body, Map.class);
			_code = (Integer) map.get("code");
			_message = (String) map.get("message");
			_comment = (String) map.get("comment");
		}
		catch(IOException parseException) {
			// Not a JSON body. Shouldn' happen.
			_code = 0;
			_message = resp.body;
			_comment = null;
		} 
		
		code = _code;
		message = _message;
		comment = _comment;
	}



}
