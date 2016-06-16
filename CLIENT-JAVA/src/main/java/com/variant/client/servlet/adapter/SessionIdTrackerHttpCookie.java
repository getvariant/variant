package com.variant.client.servlet.adapter;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.VariantInitParams;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.servlet.util.VariantCookie;
import com.variant.core.util.VariantStringUtils;

/**
 * HTTP Cookie based session ID tracker. Session ID is saved in an HTTP cookie.
 * Use HTTP request object to cache a pending session ID. In other words, if
 * there's no vrnt-ssnid cookie and we've genned a random number, save that in
 * request, so that we can reuse it on a subsequent request with the same
 * HTTP request.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class SessionIdTrackerHttpCookie implements VariantSessionIdTracker {
		
	private static final Logger LOG = LoggerFactory.getLogger(SessionIdTrackerHttpCookie.class);
	private static final Random rand = new Random(System.currentTimeMillis());
	
	private SsnIdCookie cookie = null;

	private void _initialized(HttpServletRequest request) {

		cookie = new SsnIdCookie(request);
		if (cookie == null || cookie.getValue() == null) {
			cookie = new SsnIdCookie(VariantStringUtils.random64BitString(rand));
			if (LOG.isDebugEnabled()) {
				LOG.debug("Created new variant session ID [" + cookie.getValue() + "] for HTTP session [" + request.getSession().getId());
			}
		}
		else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Retrieved existing variant session ID [" + cookie.getValue() + "] for HTTP session [" + request.getSession().getId());
			}			
		}
	}

	private void _save(HttpServletResponse response) {
		cookie.send(response);		
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	public static final String COOKIE_NAME = "vrnt-ssnid";

	/**
	 * No-argument constructor must be provided by contract.
	 */
	public SessionIdTrackerHttpCookie() {}

	/** 
	 * @return session Id.
	 */
	@Override
	public void initialized(VariantInitParams initParams, Object... userData) throws Exception {
		HttpServletRequest request = (HttpServletRequest) userData[0];
		_initialized(request);
	}
	
	public String get() {
		return cookie.getValue();
	}

	/**
	 * We expect one 
	 * @param userData
	 */
	@Override
	public void save(Object... userData) {
		HttpServletResponse response = (HttpServletResponse) userData[0];
		_save(response);
	}

	/**
	 * Session ID tracking cookie.
	 */
	private static class SsnIdCookie extends VariantCookie {

		SsnIdCookie(String sid) {
			super(COOKIE_NAME);
			super.setValue(sid);
		}
		
		SsnIdCookie(HttpServletRequest request) {
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
