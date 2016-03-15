package com.variant.web;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.util.VariantStringUtils;
import com.variant.web.util.VariantCookie;

/**
 * HTTP Cookie based session ID tracking. Session ID is saved in an HTTP cookie.
 * Use HTTP request object to cache a pending session ID. In other words, if
 * there's no vrnt-ssnid cookie and we've genned a random number, save that in
 * request, so that we can reuse it on a subsequent request from with the same
 * HTTP request.
 * 
 *
 * @author Igor Urisman
 * @since 0.6
 */
public class SessionIdTrackerHttpCookie implements VariantSessionIdTracker {
		
	public static final String COOKIE_NAME = "vrnt-ssnid";

	private static final Logger LOG = LoggerFactory.getLogger(SessionIdTrackerHttpCookie.class);
	private static final Random rand = new Random(System.currentTimeMillis());
	
	/**
	 * No-arg constructor must be provided by contract.
	 */
	public SessionIdTrackerHttpCookie() {}

	@Override
	public void initialized(InitializationParams initParams) throws Exception {}

	@Override
	public void shutdown() {}
	
	/**
	 * We expect caller to pass 2 arguments: <code>HttpServletRequest</code>
	 * and <code>HttpServletResponse</code>. If the cookie did not exist, create
	 * it and add the cookie to the response.
	 * 
	 * @return session Id.
	 */
	@Override
	public String get(Object...userData) {
		
		HttpServletRequest request = (HttpServletRequest) userData[0];
		HttpServletResponse response = (HttpServletResponse) userData[1];
		
		// Check the VRNT-SSNID cookie first.  If there - we're done.
		String result = new SsnIdCookie(request).getValue();
		if (result == null) {
			// If not in the cookie, check in the HTTP request, in case this is not the
			// first client call and we don't want to create a different session ID.
			result = (String) request.getAttribute(COOKIE_NAME);
			if (result == null) {
				// Still nothing. Really create.
				result = VariantStringUtils.random64BitString(rand);
				SsnIdCookie cookie = new SsnIdCookie();
				cookie.setValue(result);
				cookie.send(response);
				request.setAttribute(COOKIE_NAME, result);
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Created new variant session ID [" + result + "] for HTTP session [" + request.getSession().getId());
			}
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Retrieved existing variant session ID [" + result + "] for HTTP session [" + request.getSession().getId());
			}			
		}
		return result;
	}

	/**
	 * Session ID tracking cookie.
	 */
	private static class SsnIdCookie extends VariantCookie {

		private SsnIdCookie() {
			super(COOKIE_NAME);
		}
		
		private SsnIdCookie(HttpServletRequest request) {
			super(COOKIE_NAME, request);
		}

		/**
		 * Session scoped cookie.
		 */
		@Override
		protected int getMaxAge() {
			return -1;
		}
		
	}
}
