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
	public void initialized(VariantInitParams initParams) {
		// Nothing.
	}
	
	@Override
	public String get(Object... userData) {
		
		HttpServletRequest request = (HttpServletRequest) userData[0];
		cookie = new SsnIdCookie(request);
		return cookie == null || cookie.getValue() == null ?  null : cookie.getValue();
	}

	/**
	 * Save
	 * @param userData
	 */
	@Override
	public void save(String sessionId, Object... userData) {
		cookie.setValue(sessionId);
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
