package com.variant.client.net.http;

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
import com.variant.client.Connection.Status;
import com.variant.client.impl.ConnectionImpl;
import com.variant.core.util.Constants;
import com.variant.core.util.TimeUtils;

/**
 * The very bottom of the HTTP Stack. 
 * Makes actual network calls.
 */
public class HttpRemoter {

	final private static Logger LOG = LoggerFactory.getLogger(HttpRemoter.class);

	// All requests 
	final ConnectionImpl connection;
	
	/**
	 * Public construction.
	 */
	public HttpRemoter(ConnectionImpl connection) {
		this.connection = connection;
	};
	
	// The only HTTP client per Variant Client. Has built-in connection pool.
	private CloseableHttpClient client = HttpClients.createDefault();

	/**
	 * Single Entry point to all calls to the server.
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
			if (connection.getStatus() == Status.OPEN) {
				req.setHeader(Constants.HTTP_HEADER_CONNID, connection.getId());
			}
			else if (connection.getStatus() != Status.CONNECTING) {
				throw new ClientException.Internal(String.format("Unexpected status %s", connection.getStatus()));
			}
			resp = client.execute(req);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format(
						"+++ %s %s : %s (%s):", 
						req.getMethod(), req.getURI(), 
						resp.getStatusLine().getStatusCode(), 
						TimeUtils.formatDuration(System.currentTimeMillis() - start)));
				if (req instanceof HttpEntityEnclosingRequestBase) {
					HttpEntity entity = ((HttpEntityEnclosingRequestBase)req).getEntity();
					String body = entity == null ? "null" : EntityUtils.toString(entity);
					LOG.trace(">>> " + body);
				}
			}
			
			HttpResponse result = new HttpResponse(req, resp);

			if (LOG.isTraceEnabled()) LOG.trace("<<< " + result.body);

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
		try { client.close(); } catch(Throwable t) {}
	}
}
