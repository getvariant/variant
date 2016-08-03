package com.variant.client.servlet;

import javax.servlet.http.HttpServletResponse;

import com.variant.core.VariantStateRequest;

/**
 * <p>Extends bare client's {@link VariantStateRequest} to include
 * the environment bound signature {@link #commit(HttpServletResponse)}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */

public interface VariantServletStateRequest extends VariantStateRequest {
	
	/**
	 * Environment bound version of of the bare client's {@link #commit(Object...)}.
	 * 
	 * @param request Current {@link HttpServletResponse}.
	 * @since 0.6
	 */
	void commit(HttpServletResponse response);
}
