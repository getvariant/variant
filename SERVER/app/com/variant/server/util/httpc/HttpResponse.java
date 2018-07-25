package com.variant.server.util.httpc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

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
}
