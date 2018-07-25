package com.variant.server.util.httpc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

abstract public class HttpOperation {

	private static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
	private static final int DEFAULT_READ_TIMEOUT = 1000;
	
	protected final HttpURLConnection conn;
	private int connTimeout = DEFAULT_CONNECTION_TIMEOUT;
	private int readTimeout = DEFAULT_READ_TIMEOUT;
	
	HttpOperation(String url) throws MalformedURLException, IOException {
		conn = (HttpURLConnection) new URL(url).openConnection();
	}
	
	/**
	 * Exec this operation
	 * @return
	 * @throws IOException
	 */
	public HttpResponse exec() throws IOException {		
		conn.setConnectTimeout(connTimeout);
		conn.setReadTimeout(readTimeout);
		conn.connect();
		return new HttpResponse(conn);
	}
	
	//-------------------------------------------------------------------------------\\
	//                               CONCRETE METHODS
	//-------------------------------------------------------------------------------\\

	public static class Get extends HttpOperation {
		
		public Get(String url) throws MalformedURLException, IOException {
			super(url);
			conn.setRequestMethod("GET");
		}
	}
}
