package com.variant.client.gone;

import javax.servlet.http.HttpServletRequest;

import com.variant.core.InitializationParams;
import com.variant.core.VariantSessionIdTracker;

/**
 * An implementation of {@link VariantSessionIdTracker} sutable for HTTP Servlet
 * containers.  No real tracking in client's context. Instead, simply use 
 * the ID of the underlying HTTP session.
 *
 * @since 0.5
 */
public class SessionIdTrackerHttpSession implements VariantSessionIdTracker {

	@Override
	public void initialized(InitializationParams initParams) throws Exception {}

	/**
	 * @param userData is 1 element array containing the servlet request.
	 */
	@Override
	public String get(Object...userData) {
		HttpServletRequest httpRequest = (HttpServletRequest) userData[0];
		return httpRequest.getSession().getId();
	}

	@Override
	public void shutdown() {}
	
}

