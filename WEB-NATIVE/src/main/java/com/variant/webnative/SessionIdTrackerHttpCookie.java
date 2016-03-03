package com.variant.webnative;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.util.VariantStringUtils;
import com.variant.webnative.util.VariantCookie;

/**
 * HTTP Cookie based session ID tracking. Session ID is saved in an HTTP cookie.
 * Not currently used.
 *
 * @author Igor Urisman
 * @since 0.5
 */
public class SessionIdTrackerHttpCookie implements VariantSessionIdTracker {
		
	private static final Logger LOG = LoggerFactory.getLogger(SessionIdTrackerHttpCookie.class);
	private static final Random rand = new Random(System.currentTimeMillis());
	
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
		String result = new SsnIdCookie(request).getValue();
		if (result == null) {
			result = VariantStringUtils.random128BitString(rand);
			HttpServletResponse response = (HttpServletResponse) userData[1];
			SsnIdCookie cookie = new SsnIdCookie();
			cookie.setValue(result);
			cookie.send(response);
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

		private static final String COOKIE_NAME = "vrnt-ssnid";

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
