package com.variant.client.gone;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.core.InitializationParams;
import com.variant.core.VariantSession;
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

	private static final Logger LOG = LoggerFactory.getLogger(SessionStoreHttpSession.class);
	
	private final static String SSN_KEY = "vrnt-session-id";
	
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
		httpRequest.getSession().setAttribute(SSN_KEY, session);
		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("Saved session ID [%s]", session.getId()));
		}
		
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
	public VariantSession get(String sessionId, Object...userData) {		
		HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
		VariantSession result = (VariantSession) httpRequest.getSession().getAttribute(SSN_KEY);
		if (LOG.isTraceEnabled()) {
			LOG.trace(String.format("Session ID [%s] %s found", sessionId, result == null ? "not" : ""));
		}
		return result;
	}


}
