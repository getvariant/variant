package com.variant.client.servlet;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.Connection;
import com.variant.client.Session;

/**
 * <p>Servlet-aware wrapper around bare client's implementation of{@link Connection}. 
 * Overrides the {@link VariantCoreSession#targetForState(State)} method to return the servlet-aware
 * state request {@link VariantServletStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.7
 */

public interface VariantServletConnection extends Connection {
	
	public VariantServletSession getOrCreateSession(HttpServletRequest req);
	
	public VariantServletSession getSession(HttpServletRequest req);
	
}
