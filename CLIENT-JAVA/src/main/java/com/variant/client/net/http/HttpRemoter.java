package com.variant.client.net.http;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.ClientException;
import com.variant.client.InternalErrorException;

/**
 * The very bottom of the HTTP Stack. 
 * Makes actual network calls.
 */
class HttpRemoter {

	final private static Logger LOG = LoggerFactory.getLogger(HttpRemoter.class);

	/**
	 * Package construction.
	 */
	HttpRemoter(){};
	
	// The only HTTP client per Variant Client. Has built-in connection pool.
	private CloseableHttpClient client = HttpClients.createDefault();

	HttpResponse call(Requestable requestable) {
		
		long start = System.currentTimeMillis();
		CloseableHttpResponse resp = null;
		try {
			HttpUriRequest req = requestable.requestOp();
			req.setHeader("Content-Type", "application/json");
			resp = client.execute(req);
			if (LOG.isTraceEnabled()) {
				LOG.trace(String.format(
						"%s %s - %s (%s)", 
						req.getMethod(), req.getURI(), 
						resp.getStatusLine().getStatusCode(), 
						DurationFormatUtils.formatDuration(System.currentTimeMillis() - start, "mm:ss.SSS")));
			}

			HttpResponse result = new HttpResponse(req, resp);
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
	
	interface Requestable {
		HttpUriRequest requestOp() throws Exception;
	}
}
