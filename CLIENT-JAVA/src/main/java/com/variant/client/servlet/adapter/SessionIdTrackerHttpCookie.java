package com.variant.client.servlet.adapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.client.VariantInitParams;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.servlet.util.VariantCookie;

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
			
	private SsnIdCookie cookie = null;

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
	public void init(VariantInitParams initParams, Object...userData) {		
		HttpServletRequest request = (HttpServletRequest) userData[0];
		cookie = new SsnIdCookie(request);
	}
	
	@Override
	public String get() {
		return cookie == null ? null : cookie.getValue();
	}

	/**
	 * Save
	 * @param userData
	 */
	@Override
	public void set(String sessionId) {
		cookie.setValue(sessionId);
	}

	/**
	 * Save
	 * @param userData
	 */
	@Override
	public void save(Object... userData) {
		HttpServletResponse response = (HttpServletResponse) userData[0];
		cookie.send(response);		
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
