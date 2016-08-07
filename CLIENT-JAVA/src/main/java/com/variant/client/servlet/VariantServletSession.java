package com.variant.client.servlet;

import com.variant.core.VariantSession;
import com.variant.core.schema.State;

/**
 * <p>Servlet-aware wrapper around bare client's {@link VariantSession} which overrides
 * the {@link VariantSession#targetForState(State)} method to return the servlet-aware
 * state request {@link VariantServletStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantServletSession extends VariantSession {

	/**
	 * Override the bare {@link VariantSession#targetForState(State)} in order to
	 * return the servlet-aware state request {@link VariantServletStateRequest}
	 * 
	 * @param state Variant {@link State} for which this session is to be targeted.
	 * 
	 * @since 0.6
	 */
	public VariantServletStateRequest targetForState(State state);

}
