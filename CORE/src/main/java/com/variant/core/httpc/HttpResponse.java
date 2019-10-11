package com.variant.core.httpc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.error.ServerError;
import com.variant.core.util.IoUtils;

/**
 * Response returned by an HttpOperation
 *
 */
public class HttpResponse {

	final public int responseCode;
	final public Optional<String> bodyString;

	final private HttpURLConnection conn;
	
	HttpResponse(HttpURLConnection conn) throws IOException {
		
		this.conn = conn;
		
		BufferedInputStream bodyStream = null;
		if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
			bodyStream = new BufferedInputStream(conn.getInputStream());
		} else {
			bodyStream = new BufferedInputStream(conn.getErrorStream());
		}
		bodyString = bodyStream == null ? Optional.empty() : Optional.of(IoUtils.toString(bodyStream));
		
		responseCode = conn.getResponseCode();
	}
		
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public String getErrorContent() throws IOException {
		
		ObjectMapper jacksonDataMapper = new ObjectMapper();
		jacksonDataMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		
		Map<String, ?> map = null;
				
		// Reconstitute the server error.
		map = jacksonDataMapper.readValue(bodyString.get(), Map.class);
		Integer code = (Integer) map.get("code");
		//boolean isInternal = (Boolean) map.get("isInternal");
		List<String> args = (List<String>) map.get("args");
		ServerError error = ServerError.byCode(code);

		return error.asMessage(args.toArray());
	}
}
