package com.variant.web.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.variant.core.exception.VariantInternalException;

public class HttpClient {

	CloseableHttpClient client = HttpClients.createDefault();
	
	public HttpClient() {}
	
	/**
	 * Send a GET with an optional body.
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse get(String url) { 		
		CloseableHttpResponse resp = null;
		try {
			HttpGet get = new HttpGet(url);
			resp = client.execute(get);
			// Apache said:
			// The underlying HTTP connection is still held by the response object
			// to allow the response content to be streamed directly from the network socket.
			// In order to ensure correct deallocation of system resources
			// the user MUST call CloseableHttpResponse#close() from a finally clause.
			// Please note that if response content is not fully consumed the underlying
			// connection cannot be safely re-used and will be shut down and discarded
			// by the connection manager.
			return new HttpResponse(get, resp);
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to perform HTTP GET", e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	} 

	public HttpResponse put(String url, String body) {
		CloseableHttpResponse resp = null;
		try {
			HttpPut put = new HttpPut(url);
			put.setHeader("Content-Type", "application/json");
			put.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
			resp = client.execute(put);
		    return new HttpResponse(put, resp);
		}
		catch (Exception e) {
			throw new VariantInternalException("Unable to perform HTTP POST", e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	}
}
