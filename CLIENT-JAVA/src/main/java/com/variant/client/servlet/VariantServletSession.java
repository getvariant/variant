package com.variant.client.servlet;

import com.variant.core.VariantSession;
import com.variant.core.schema.State;

/**
 * <p>Extends bare client's {@link VariantSession} to include
 * the environment bound signature {@link #targetForState(State)}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public interface VariantServletSession extends VariantSession {

	public VariantServletStateRequest targetForState(State state);

}
