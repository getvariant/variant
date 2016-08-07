package com.variant.client.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.client.VariantClient;
import com.variant.client.VariantInitParams;
import com.variant.client.VariantSessionIdTracker;
import com.variant.client.servlet.util.VariantCookie;
import com.variant.core.VariantStateRequest;

/**
 * Concrete implementation of Variant session ID tracker based on HTTP cookie. 
 * Session ID is saved in a browser session-scoped
 * cookie between state requests.
 * 
 * @author Igor Urisman
 * @since 0.5
 */
public class SessionIdTrackerHttpCookie implements VariantSessionIdTracker {
			
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

	private SsnIdCookie cookie = null;

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	public static final String COOKIE_NAME = "variant-ssnid";

	/**
	 * No-argument constructor must be provided by contract. Called by Variant client within the scope
	 * of the {@link VariantClient#getSession(Object...)} call.
	 */
	public SessionIdTrackerHttpCookie() {}

	/**
	 * <p>Called by Variant to initialize a new instance, within the scope of the 
	 * {@link VariantClient#getSession(Object...)} method. Use this to inject state from configuration.
	 * 
	 * @param initParams An instance of type {@link VariantInitParams}, containing parsed JSON object, 
	 *                   specified by the <code>session.id.tracker.class.init</code> application property.
	 * @param userData   This implementation expects userData to be a one-element array whose single element
	 *                   is the current {@link HttpServletRequest}.
	 *
	 * @since 0.6
	 */
	@Override
	public void init(VariantInitParams initParams, Object...userData) {		
		HttpServletRequest request = (HttpServletRequest) userData[0];
		cookie = new SsnIdCookie(request);
	}
	
	/**
	 * <p>Retrieve the current value of the session ID from this tracker. 
	 * This value may have been set by {@link #init(VariantInitParams, Object...)} or by {@link #set(String)}.
	 * 
	 * @return Session ID, if present in the tracker or null otherwise.
	 * @since 0.6
	 */
	@Override
	public String get() {
		return cookie == null ? null : cookie.getValue();
	}

	/**
	 * <p>Set the value of session ID. Use to start tracking a new session.
	 * 
	 * @param sessionId Session ID to set.
	 * @since 0.6
	 */
	@Override
	public void set(String sessionId) {
		cookie.setValue(sessionId);
	}

	/**
	 * <p>Called by Variant to save the current value of session ID to the underlying persistence mechanism. 
	 * Variant client calls this method within the scope of the {@link VariantStateRequest#commit(Object...)} method.
	 * 
	 * @param userData This implementation expects userData to be a one-element array whose single element
	 *                   is the current {@link HttpServletResponse}.
	 *                 
	 * @since 0.6
	 */
	@Override
	public void save(Object... userData) {
		HttpServletResponse response = (HttpServletResponse) userData[0];
		cookie.send(response);		
	}
}
