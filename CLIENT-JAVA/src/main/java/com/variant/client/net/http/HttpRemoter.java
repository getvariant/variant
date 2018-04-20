package com.variant.client.net.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.impl.ConnectionImpl;
import com.variant.core.ConnectionStatus;
import com.variant.core.util.Constants;
import com.variant.core.util.TimeUtils;

/**
 * The very bottom of the HTTP Stack.
 * Owns an instance of an HTTP Client and makes actual network calls.
 * One per Variant Client 
 */
public class HttpRemoter {

	final private static Logger LOG = LoggerFactory.getLogger(HttpRemoter.class);

	// The only HTTP client per Variant Client. Has built-in connection pool.
	final private CloseableHttpClient httpClient;
	
	/**
	 * Public construction.  
	 */
	public HttpRemoter() {
		 httpClient = HttpClients.createDefault();		
	};
	

	/**
	 * Single Entry point to all unconnected calls to the server.
	 * Should only be used for obtaining a connection.
	 * 
	 * @param requestable
	 * @return
	 */
	HttpResponse call(Requestable requestable) {
		
		long start = System.currentTimeMillis();
		CloseableHttpResponse resp = null;
		try {
			HttpUriRequest req = requestable.requestOp();
			req.setHeader("Content-Type", Constants.HTTP_HEADER_CONTENT_TYPE);

			resp = httpClient.execute(req);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format(
						"+++ %s %s : %s (%s) responded with:", 
						req.getMethod(), req.getURI(), 
						resp.getStatusLine().getStatusCode(), 
						TimeUtils.formatDuration(System.currentTimeMillis() - start)));
				if (req instanceof HttpEntityEnclosingRequestBase) {
					HttpEntity entity = ((HttpEntityEnclosingRequestBase)req).getEntity();
					String body = entity == null ? "null" : EntityUtils.toString(entity);
					LOG.trace("Body: '" + body + "'");
					LOG.trace("Connection Status: " + resp.getHeaders(Constants.HTTP_HEADER_CONN_STATUS)[0]);
				}
			}
			
			HttpResponse result = new HttpResponse(req, resp);

			if (LOG.isTraceEnabled()) {
				LOG.trace("Body: '" + result.body + "'");
			}

			// Process the http status and the body.
			switch (result.status) {
			
			case HttpStatus.SC_OK:
				return result;
				
			case HttpStatus.SC_BAD_REQUEST:
				
				if (LOG.isDebugEnabled()) {
					LOG.debug("Server status " + result.status + ": " + result.body);
				}
				throw result.toClientException();
			
			default:
				throw new ClientException.Internal(
						String.format("Unexpected response from server: [%s %s : %s]",
								req.getMethod(), req.getURI(), result.status)
				);
			}
		}
		catch (ClientException ce) {
			throw ce;
		}
		catch (Throwable e) {
			throw new ClientException.Internal("Unexpected exception in HTTP POST: " + e.getMessage(), e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	}

	/**
	 * Single Entry point to all connected calls to the server.
	 * 
	 * @param requestable
	 * @return
	 */
	HttpResponse call(Requestable requestable, Connection conn) {
		
		long start = System.currentTimeMillis();
		CloseableHttpResponse resp = null;
		try {
			HttpUriRequest req = requestable.requestOp();
			req.setHeader("Content-Type", Constants.HTTP_HEADER_CONTENT_TYPE);
			if (conn.getStatus() == ConnectionStatus.OPEN) {
				req.setHeader(Constants.HTTP_HEADER_CONNID, conn.getId());
			}
			else {
				throw new ClientException.Internal(String.format("Unexpected status %s", conn.getStatus()));
			}
			resp = httpClient.execute(req);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format(
						"+++ %s %s : %s (%s) responded with:", 
						req.getMethod(), req.getURI(), 
						resp.getStatusLine().getStatusCode(), 
						TimeUtils.formatDuration(System.currentTimeMillis() - start)));
				if (req instanceof HttpEntityEnclosingRequestBase) {
					HttpEntity entity = ((HttpEntityEnclosingRequestBase)req).getEntity();
					String body = entity == null ? "null" : EntityUtils.toString(entity);
					LOG.trace("Body: '" + body + "'");
					LOG.trace("Connection Status: " + resp.getHeaders(Constants.HTTP_HEADER_CONN_STATUS)[0]);
				}
			}
			
			HttpResponse result = new HttpResponse(req, resp);

			Header[] connStatusHeader = resp.getHeaders(Constants.HTTP_HEADER_CONN_STATUS);
			String connStatus = connStatusHeader.length == 0 ? null : connStatusHeader[0].getValue();

			if (LOG.isTraceEnabled()) {
				LOG.trace("Body: '" + result.body + "'");
				LOG.trace("Connection Status: " + (connStatus == null ? "null" : connStatus));
			}

			// If the server sent the connection status header, update this connection.
			if (connStatus != null) ((ConnectionImpl)conn).setStatus(ConnectionStatus.valueOf(connStatus));

			// Process the http status and the body.
			switch (result.status) {
			case HttpStatus.SC_OK:
				return result;
			case HttpStatus.SC_BAD_REQUEST:
				
				if (LOG.isDebugEnabled()) {
					LOG.debug("Server status " + result.status + ": " + result.body);
				}
				throw result.toClientException();
			default:
				throw new ClientException.Internal(
						String.format("Unexpected response from server: [%s %s : %s]",
								req.getMethod(), req.getURI(), result.status)
				);
			}
		}
		catch (ClientException ce) {
			throw ce;
		}
		catch (Throwable e) {
			throw new ClientException.Internal("Unexpected exception in HTTP POST: " + e.getMessage(), e);
		} finally {
			if (resp != null) {
				try {resp.close();}					
				catch (Exception e) {}
			}
		}		
	}
	
	/**
	 */
	interface Requestable {
		HttpUriRequest requestOp() throws Exception;
	}
	
	/**
	 * Close the HTTP client gracefully, in order to avoid hangup errors in the log,
	 * because the HTTP Client may hold connections open for optimization.
	 */
	public void destroy() {
		try { httpClient.close(); } catch(Throwable t) {}
	}
}
