package com.variant.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;

public class SessionStoreHttpSession implements VariantSessionStore {

	private static String SESSION_ATTR_KEY = "VARIANT_SESSION";
	private static SessionIdTrackerImpl sidTracker = new SessionIdTrackerImpl();

	/**
	 * 
	 */
	private static class SessionIdTrackerImpl implements VariantSessionIdTracker {

		/**
		 * No real tracker. The session is stored in the http session as a parameter.
		 * We still want the session id to be unique, so we use the http session id.
		 */
		@Override
		public String get(Object...userData) {
			HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
			return httpRequest.getSession().getId();
		}
		
	}
	
	@Override
	public void shutdown() {
		// nothing.
		
	}

	@Override
	public void save(VariantSession session, Object...userData) {
		HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
		// We don't need the response, but other implementations may,
		// e.g. when we track our own session in cookie.
		// HttpServletResponse httpResponse = (HttpServletResponse) userData[1];
		httpRequest.getSession().setAttribute(SESSION_ATTR_KEY, session);
	}

	@Override
	public VariantSession get(Object...userData) {		
		HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
		return (VariantSession) httpRequest.getSession().getAttribute(SESSION_ATTR_KEY);
	}

	@Override
	public VariantSessionIdTracker getSessionIdTracker() {
		return sidTracker;
	}

}
