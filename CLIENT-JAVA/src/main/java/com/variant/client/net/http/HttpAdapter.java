package com.variant.client.net.http;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;

public class HttpAdapter {
		
	private final HttpRemoter remoter;
	
	// ---------------------------------------------------------------------------------------------//
	//                                             PUBLIC                                           //
	// ---------------------------------------------------------------------------------------------//

	public HttpAdapter(HttpRemoter remoter) {
		this.remoter = remoter;
	}
	
	/**
	 * Send a GET.
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse get(final String url) { 		
		
		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() {
						return new HttpGet(url);
					}
				}
		);
	} 

	/**
	 * Send a POST with a body
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse post(final String url, final String body) {
		
		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() throws Exception {
						HttpPost post = new HttpPost(url);
						post.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
						return post;
					}
				}
		);			
	}
	
	/**
	 * Send a POST without a body
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse post(final String url) {

		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() {
						return new HttpPost(url);
					}
				}
		);
	}

	/**
	 * Send a PUT.
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse put(final String url, final String body) {
		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() throws Exception {
						HttpPut put = new HttpPut(url);
						put.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
						return put;
					}
				}
		);
	}

	/**
	 * Send a DELETE without a body
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse delete(final String url) {
		
		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() {
						return new HttpDelete(url);
					}
				}
		);
	}

	
}
