package com.variant.web.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.variant.core.exception.VariantInternalException;

public class HttpClient {

	CloseableHttpClient client = HttpClients.createDefault();
	
	public HttpClient() {}
	
	public HttpResponse get(String url) { 
		
		HttpResponse result = null;
		CloseableHttpResponse resp = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			resp = client.execute(httpGet);
			// Apache said:
			// The underlying HTTP connection is still held by the response object
			// to allow the response content to be streamed directly from the network socket.
			// In order to ensure correct deallocation of system resources
			// the user MUST call CloseableHttpResponse#close() from a finally clause.
			// Please note that if response content is not fully consumed the underlying
			// connection cannot be safely re-used and will be shut down and discarded
			// by the connection manager. 
		    result = new HttpResponse(resp);
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to perform HTTP GET", e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}
		
		return result;
	} 

}
