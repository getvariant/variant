package com.variant.client.servlet;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.Connection;
import com.variant.core.schema.State;

/**
 * <p>Servlet-aware wrapper around bare client's implementation of{@link Connection}. 
 * Overrides the {@link VariantCoreSession#targetForState(State)} method to return the servlet-aware
 * state request {@link ServletStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.7
 */

public interface ServletConnection extends Connection {
	
	// Narrow return type of inherited methods
	@Override
	ServletSession getOrCreateSession(Object... userData);

	@Override
	ServletSession getSession(Object... userData);

	@Override
	ServletSession getSessionById(String sessionId);

	// New methods with servlet-aware signatures.
	// Inherited methods will delegate to one of these.

	ServletSession getOrCreateSession(HttpServletRequest req);
	
	ServletSession getSession(HttpServletRequest req);
	
}
