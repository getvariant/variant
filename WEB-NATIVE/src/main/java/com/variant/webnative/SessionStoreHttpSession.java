package com.variant.webnative;

import javax.servlet.http.HttpServletRequest;

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
		httpRequest.getSession().setAttribute(session.getId(), session);
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
		return (VariantSession) httpRequest.getSession().getAttribute(sessionId);
	}


}
