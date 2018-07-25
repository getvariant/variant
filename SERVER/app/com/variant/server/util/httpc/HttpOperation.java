package com.variant.server.util.httpc;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

abstract public class HttpOperation {

	private static final int DEFAULT_CONNECTION_TIMEOUT = 2000;
	private static final int DEFAULT_READ_TIMEOUT = 1000;
	
	protected final HttpURLConnection conn;
	private int connTimeout = DEFAULT_CONNECTION_TIMEOUT;
	private int readTimeout = DEFAULT_READ_TIMEOUT;
	
	/**
	 * 
	 * @param url
	 */
	HttpOperation(String url) {
		HttpURLConnection connResult = null;
		try {
			connResult = (HttpURLConnection) new URL(url).openConnection();
		}
		catch (Throwable t) {
			throw new RuntimeException("Unable to connect to [" + url + "]", t);
		}
		conn = connResult;
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
	//                            Factory Methods
	//-------------------------------------------------------------------------------\\
	
	public static HttpOperation get(String url) {
		return new Get(url);
	}
	//-------------------------------------------------------------------------------\\
	//                            CONCRETE OPERATIONS
	//-------------------------------------------------------------------------------\\

	public static class Get extends HttpOperation {
		
		private Get(String url) {
			super(url);
			try {
				conn.setRequestMethod("GET");
			}
			catch (Throwable t) {
				throw new RuntimeException("Unable to set GET", t);
			}
		}
	}
}
