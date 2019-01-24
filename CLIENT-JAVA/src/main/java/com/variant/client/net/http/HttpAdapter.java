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

	public HttpAdapter() {
		this.remoter = new HttpRemoter();
	}
	
	/**
	 * Send a GET without a body
	 * @param url
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
	 * *********** COMMENTED OUT: JAVASCRIPT DOES NOT SUPPORT GET WITH BODY **************
	 * Send a GET with a body. Note that the stock HttpGet does not support entities
	 * (bodies) so we've created our oun HttpGetWithEntity class.
	 * @param url
	 * @param body
	 * @return
	 *
	public HttpResponse get(final String url, final String body, Connection conn) { 		
		
		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() throws Exception {
						HttpGetWithEntity get =  new HttpGetWithEntity(url);
						get.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
						return get;
					}
				},
				conn
		);
	} 
	***************************************************************************************/
	
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
	 * Unconnected post -- special case only used to get the connection.
	 * @param url
	 * @param body
	 * @return
	 *
	public HttpResponse post(final String url) {

		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() {
						return new HttpPost(url);
					}
				}
		);
	}
*/
	/**
	 * Send a PUT with a body.
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

	/**
	 * Send a DELETE with a body
	 * @param url
	 * @param body
	 * @return
	 */
	public HttpResponse delete(final String url, final String body) {
		
		return remoter.call(
				new HttpRemoter.Requestable() {	
					@Override public HttpUriRequest requestOp() throws Exception {
						HttpDeleteWithEntity delete = new HttpDeleteWithEntity(url);
						delete.setEntity(new ByteArrayEntity(body.getBytes("UTF-8")));
						return delete;
					}
				}
		);
	}

}
