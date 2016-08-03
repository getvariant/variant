package com.variant.client.servlet.impl;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.variant.client.impl.ClientStateRequestImpl;
import com.variant.client.servlet.VariantServletSession;
import com.variant.client.servlet.VariantServletStateRequest;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.impl.CoreStateRequestImpl;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * <p>{@link VariantStateRequest} implementation used by the Servlet Adapter.
 * Replaces bare client's {@link VariantStateRequest#commit(Object...)} with the
 * environment bound signature {@link #commit(HttpServletResponse)}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */

public class ServletStateRequestImpl implements VariantServletStateRequest {

	private VariantStateRequest bareRequest;
	private VariantServletSession servletSession;
	
	// ---------------------------------------------------------------------------------------------//
	//                                      PUBLIC AUGMENTED                                        //
	// ---------------------------------------------------------------------------------------------//

	public ServletStateRequestImpl(VariantStateRequest bareRequest, VariantServletSession servletSession) {
		if (bareRequest == null) throw new VariantInternalException("Bare state request cannot be null");
		if (servletSession == null) throw new VariantInternalException("Servlet session cannot be null");
		this.bareRequest = bareRequest;
		this.servletSession = servletSession;
	}
	
	@Override
	public void commit(Object... userData) {
		if (userData.length != 1)
			throw new IllegalArgumentException("Invalid user data: single element vararg expected");
		else if (!(userData[0] instanceof HttpServletResponse))
			throw new IllegalArgumentException("Invalid user data: HttpServletResponse expected");

		bareRequest.commit(userData);
	}

	@Override
	public void commit(HttpServletResponse response) {
		bareRequest.commit(response);
	}

	@Override
	public VariantServletSession getSession() {
		return servletSession;
	}

	// ---------------------------------------------------------------------------------------------//
	//                                      PUBLIC PASS-THRU                                        //
	// ---------------------------------------------------------------------------------------------//

	@Override
	public State getState() {
		return bareRequest.getState();
	}

	@Override
	public Map<String, String> getResolvedParameterMap() {
		return bareRequest.getResolvedParameterMap();
	}

	@Override
	public Collection<Experience> getActiveExperiences() {
		return bareRequest.getActiveExperiences();
	}

	@Override
	public Experience getActiveExperience(Test test) {
		return bareRequest.getActiveExperience(test);
	}

	@Override
	public VariantEvent getStateVisitedEvent() {
		return bareRequest.getStateVisitedEvent();
	}

	@Override
	public void setStatus(Status status) {
		bareRequest.setStatus(status);
	}

	@Override
	public boolean isCommitted() {
		return bareRequest.isCommitted();
	}

	@Override
	public Status getStatus() {
		return bareRequest.getStatus();
	}

	// ---------------------------------------------------------------------------------------------//
	//                                    PUBLIC EXT PASS-THRU                                      //
	// ---------------------------------------------------------------------------------------------//

	public CoreStateRequestImpl getCoreStateRequest () {
		return ((ClientStateRequestImpl)bareRequest).getCoreStateRequest();
	}

}
