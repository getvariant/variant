package com.variant.web;

import javax.servlet.http.HttpServletRequest;

import com.variant.core.InitializationParams;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;

/**
 * An implementation of {@link VariantSessionStore} used by Variant Web API.
 * Variant session is kept in the underlying HTTP session. This approach works
 * so long as the underlying HTTP session is not recreated by the target application
 * during the life of Variant session. 
 *
 * @author Igor Urisman
 * @since 0.5
 */
public class SessionStoreHttpSession implements VariantSessionStore {

	private static String SESSION_ATTR_KEY = "VARIANT_SESSION";
	private static SessionIdTrackerImpl sidTracker = new SessionIdTrackerImpl();

	/**
	 * An implementation of {@link VariantSessionIdTracker} used by this session store
	 * implementation. 
	 *
	 * @since 0.5
	 */
	private static class SessionIdTrackerImpl implements VariantSessionIdTracker {

		/**
		 * No real tracker. The session is stored in the HTTP session as a parameter,
		 * so there's no need to track session ID in client state. We still want the 
		 * session id to be unique, for the purposes of experiment analysis, so we use 
		 * the HTTP session ID as Variant session ID.
		 */
		@Override
		public String get(Object...userData) {
			HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
			return httpRequest.getSession().getId();
		}
		
	}
	
	@Override
	public void initialized(InitializationParams initParams) throws Exception {}

	@Override
	public void shutdown() {}

	/**
	 * Save Variant session in the HTTP session.
	 * 
	 * @param session Variant session to be saved.
	 * @param userData An object array whose first and only element must be the current
	 *                 <code>HttpServletRequest</code>.
	 * @since 0.5
	 */
	@Override
	public void save(VariantSession session, Object...userData) {
		HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
		// We don't need the response, but other implementations may,
		// e.g. when we track our own session in cookie.
		// HttpServletResponse httpResponse = (HttpServletResponse) userData[1];
		httpRequest.getSession().setAttribute(SESSION_ATTR_KEY, session);
	}

	/**
	 * Get current Variant session from the HTTP session.
	 * 
	 * @param userData An object array whose first and only element must be the current
	 *                 <code>HttpServletRequest</code>.
	 * @return Variant session.
	 * @since 0.5
	 */	
	@Override
	public VariantSession get(Object...userData) {		
		HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
		return (VariantSession) httpRequest.getSession().getAttribute(SESSION_ATTR_KEY);
	}

	/**
	 * Session ID tracker.
	 * 
	 * @return An instance of {@link SessionIdTrackerImpl}.
	 * @since 0.5
	 */	
	@Override
	public VariantSessionIdTracker getSessionIdTracker() {
		return sidTracker;
	}

}
