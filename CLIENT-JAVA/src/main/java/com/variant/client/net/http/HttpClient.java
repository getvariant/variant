package com.variant.client.net.http;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.ClientException;
import com.variant.client.InternalErrorException;

public class HttpClient {

	final private static Logger LOG = LoggerFactory.getLogger(HttpClient.class);
	
	private CloseableHttpClient client = HttpClients.createDefault();
	
	public HttpClient() {}
	
	/**
	 * Send a GET.
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse get(String url) { 		
		
		CloseableHttpResponse resp = null;
		long start = System.currentTimeMillis();
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
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format("GET %s : %s in %s", url, resp.getStatusLine(), DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS")));
			}
			return new HttpResponse(get, resp);
		}
		catch (Exception e) {
			throw new InternalErrorException("Unexpected exception in HTTP GET: " + e.getMessage(), e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	} 

	/**
	 * Send a POST with a body
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse post(String url, String body) {
		
		CloseableHttpResponse resp = null;
		long start = System.currentTimeMillis();
		try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			post.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
			resp = client.execute(post);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format("POST %s [%s] : %s in %s", url, body, resp.getStatusLine(), DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS")));
			}
		    return new HttpResponse(post, resp);
		}
		catch (Exception e) {
			throw new InternalErrorException("Unexpected exception in HTTP POST: " + e.getMessage(), e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	}
	
	/**
	 * Send a POST without a body
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse post(String url) {
		
		CloseableHttpResponse resp = null;
		long start = System.currentTimeMillis();
		try {
			HttpPost post = new HttpPost(url);
			post.setHeader("Content-Type", "application/json");
			resp = client.execute(post);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format("POST %s : %s in %s", url, resp.getStatusLine(), DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS")));
			}
			HttpResponse result = new HttpResponse(post, resp);
			switch (result.status) {
			case HttpStatus.SC_OK:
				return result;
			case HttpStatus.SC_BAD_REQUEST:
			case HttpStatus.SC_INTERNAL_SERVER_ERROR:
				throw result.toClientException();
			default:
				throw new InternalErrorException("Bad response from server [" + result.toString() + "]");
			}
		}
		catch (ClientException ce) {
			throw ce;
		}
		catch (Throwable e) {
			throw new InternalErrorException("Unexpected exception in HTTP POST: " + e.getMessage(), e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	}

	/**
	 * Send a PUT.
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse put(String url, String body) {
		
		CloseableHttpResponse resp = null;
		long start = System.currentTimeMillis();
		try {
			HttpPut put = new HttpPut(url);
			put.setHeader("Content-Type", "application/json");
			put.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
			resp = client.execute(put);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format("PUT %s [%s] : %s in %s", url, body, resp.getStatusLine(), DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS")));
			}
		    return new HttpResponse(put, resp);
		}
		catch (Exception e) {
			throw new InternalErrorException("Unexpected exception in HTTP PUT: " + e.getMessage(), e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	}

}
