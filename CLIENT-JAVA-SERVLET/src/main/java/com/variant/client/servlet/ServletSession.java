package com.variant.client.servlet;

import com.variant.client.Session;
import com.variant.core.schema.State;

/**
 * <p>Servlet-aware wrapper around bare client's {@link VariantCoreSession} which overrides
 * the {@link VariantCoreSession#targetForState(State)} method to return the servlet-aware
 * state request {@link ServletStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface ServletSession extends Session {

	/**
	 * Override the bare {@link VariantCoreSession#targetForState(State)} in order to
	 * return the servlet-aware state request {@link ServletStateRequest}
	 * 
	 * @param state Variant {@link State} for which this session is to be targeted.
	 * 
	 * @since 0.6
	 */
	public ServletStateRequest targetForState(State state);

}
