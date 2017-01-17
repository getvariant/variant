package com.variant.client.servlet;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.Connection;
import com.variant.core.schema.State;

/**
 * <p>Servlet-aware wrapper around bare client's implementation of{@link Connection}. 
 * Overrides the {@link VariantCoreSession#targetForState(State)} method to return the servlet-aware
 * state request {@link VariantServletStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.7
 */

public interface VariantServletConnection extends Connection {
	
	// Narrow return type of inherited methods
	@Override
	VariantServletSession getOrCreateSession(Object... userData);

	@Override
	VariantServletSession getSession(Object... userData);

	@Override
	VariantServletSession getSessionById(String sessionId);

	// New methods with servlet-aware signatures.
	// Inherited methods will delegate to one of these.

	VariantServletSession getOrCreateSession(HttpServletRequest req);
	
	VariantServletSession getSession(HttpServletRequest req);
	
}
