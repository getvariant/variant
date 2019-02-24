package com.variant.server.util.httpc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.core.error.ServerError;
import com.variant.core.util.IoUtils;

/**
 * Response returned by an HttpOperation
 *
 */
public class HttpResponse {

	final private int rc;
	final private BufferedInputStream bodyStream;
	final private HttpURLConnection conn;
	
	HttpResponse(HttpURLConnection conn) throws IOException {
		
		this.conn = conn;
		
		if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
			bodyStream = new BufferedInputStream(conn.getInputStream());
		} else {
			bodyStream = new BufferedInputStream(conn.getErrorStream());
		}
		rc = conn.getResponseCode();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getResponseCode() {
		return rc;
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getStringContent() throws IOException {
		return bodyStream == null ? null : IoUtils.toString(bodyStream);
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
		map = jacksonDataMapper.readValue(getStringContent(), Map.class);
		Integer code = (Integer) map.get("code");
		//boolean isInternal = (Boolean) map.get("isInternal");
		List<String> args = (List<String>) map.get("args");
		ServerError error = ServerError.byCode(code);

		return error.asMessage(args.toArray());
	}
}
