package com.variant.client.servlet;

import com.variant.client.VariantSession;
import com.variant.core.VariantCoreSession;
import com.variant.core.xdm.State;

/**
 * <p>Servlet-aware wrapper around bare client's {@link VariantCoreSession} which overrides
 * the {@link VariantCoreSession#targetForState(State)} method to return the servlet-aware
 * state request {@link VariantServletStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantServletSession extends VariantSession {

	/**
	 * Override the bare {@link VariantCoreSession#targetForState(State)} in order to
	 * return the servlet-aware state request {@link VariantServletStateRequest}
	 * 
	 * @param state Variant {@link State} for which this session is to be targeted.
	 * 
	 * @since 0.6
	 */
	public VariantServletStateRequest targetForState(State state);

}
