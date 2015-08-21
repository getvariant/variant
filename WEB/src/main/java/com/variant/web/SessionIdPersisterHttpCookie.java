package com.variant.web;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.session.SessionIdPersister;
import com.variant.core.util.VariantStringUtils;
import com.variant.web.util.VariantCookie;


/**
 * HTTP Cookie based session ID tracking. Session ID is saved in an HTTP cookie.
 * 
 * @author Igor
 *
 */
public class SessionIdPersisterHttpCookie implements SessionIdPersister {
		
	private static final Logger LOG = LoggerFactory.getLogger(SessionIdPersisterHttpCookie.class);
	private static final Random rand = new Random(System.currentTimeMillis());
	
	/**
	 * We expect caller to pass 1 argument of type <code>HttpServletRequest</code>
	 * @return session Id, if existed, or null otherwise.
	 */
	public String get(Object userData) {
		HttpServletRequest request = (HttpServletRequest) userData;
		String result = new SsnIdCookie(request).getValue();
		if (result == null) {
			result = VariantStringUtils.random128BitString(rand);
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
	 * We expect caller to pass 1 argument of type <code>HttpServletResponse</code>
	 */
	public void persist(String sid, Object userData) {
		HttpServletResponse response = (HttpServletResponse) userData;
		SsnIdCookie cookie = new SsnIdCookie();
		cookie.setValue(sid);
		cookie.send(response);
	}

	/**
	 *
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
