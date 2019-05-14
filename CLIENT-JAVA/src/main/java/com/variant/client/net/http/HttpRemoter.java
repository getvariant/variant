package com.variant.client.net.http;

import static com.variant.client.impl.ConfigKeys.SYS_PROP_TIMERS;

import java.time.Duration;
import java.time.Instant;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.ServerConnectException;
import com.variant.client.VariantException;
import com.variant.client.util.Timers;
import com.variant.core.Constants;
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
		
		Instant start = Instant.now();
		HttpUriRequest req = null;
		CloseableHttpResponse resp = null;
		try {
			req = requestable.requestOp();
			req.setHeader("Content-Type", Constants.HTTP_HEADER_CONTENT_TYPE);

			resp = httpClient.execute(req);			
			HttpResponse result = new HttpResponse(req, resp);

			if (System.getProperty(SYS_PROP_TIMERS) != null) {
				Timers.remoteTimer.get().increment();
				Header[] timerHeaderArr = resp.getHeaders(Constants.HTTP_HEADER_SERVER_TIMIER);
				String millisString = 
					timerHeaderArr == null || timerHeaderArr.length == 0 ? "0" : timerHeaderArr[0].getValue();
				Timers.remoteTimer.get().increment(Long.parseLong(millisString));
			}
			
			Duration elapsed = Duration.between(start, Instant.now());
			
			if (LOG.isTraceEnabled()) {
				
				StringBuilder buff = new StringBuilder();
				buff.append("\n>>> ").append(req.getMethod()).append(" ").append(req.getURI());
				if (req instanceof HttpEntityEnclosingRequestBase) {
					HttpEntity entity = ((HttpEntityEnclosingRequestBase)req).getEntity();
					String body = entity == null ? "null" : EntityUtils.toString(entity);
					buff.append("\nBody: '").append(body).append("'");
				}
				buff.append("\n<<< ").append(resp.getStatusLine().getStatusCode())
					.append(" (")
					.append(TimeUtils.formatDuration(elapsed))
					.append(")");
				buff.append("\nConnection Status: ").append(result.status);
				buff.append("\nBody: '").append(result.body).append("'");
				
				LOG.trace(buff.toString());
			}
			
			// Process the http status and the body.
			switch (result.status) {
			
			case HttpStatus.SC_OK:
				return result;
				
			case HttpStatus.SC_BAD_REQUEST:
				
				com.variant.core.error.VariantException ce = result.toVariantException();
				if (LOG.isDebugEnabled()) LOG.debug("Server Error " + ce.getMessage());
				throw ce;
			
			default:
				throw new VariantException.Internal(
						String.format("Unexpected response from server: [%s %s : %s]",
								req.getMethod(), req.getURI(), result.status)
				);
			}
		}
		catch (VariantException ex) {
			throw ex;
		}
		catch (HttpHostConnectException ex) {
			// To get the server, only get the URI prefix ending with a single slash.
			throw new ServerConnectException(req.getURI().getHost());
		}
		catch (Throwable e) {
			throw new VariantException.Internal("Unexpected exception in [" + req.getRequestLine() + "]", e);
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
