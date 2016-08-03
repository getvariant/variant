package com.variant.client.servlet.impl;

import java.util.Collection;

import com.variant.client.servlet.VariantServletSession;
import com.variant.client.servlet.VariantServletStateRequest;
import com.variant.client.session.ClientSessionImpl;
import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.util.Tuples.Pair;

/**
 * <p>{@link VariantSession} implementation used by the Servlet Adapter.
 * Replaces bare client's {@link ClientSessionImpl#targetForState(com.variant.core.schema.State)} 
 * method with one, which returns a custom implementation of {@link VariantStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class ServletSessionImpl implements VariantServletSession {

	private VariantSession bareSession;
	private VariantServletStateRequest servletStateRequest;
	
	// ---------------------------------------------------------------------------------------------//
	//                                      PUBLIC AUGMENTED                                        //
	// ---------------------------------------------------------------------------------------------//

	@Override
	public VariantServletStateRequest targetForState(State state) {
		servletStateRequest = new ServletStateRequestImpl(bareSession.targetForState(state), this);
		return servletStateRequest;
	}

	@Override
	public VariantStateRequest getStateRequest() {
		return servletStateRequest;
	}

	// ---------------------------------------------------------------------------------------------//
	//                                      PUBLIC PASS-THRU                                        //
	// ---------------------------------------------------------------------------------------------//

	public ServletSessionImpl(VariantSession bareSession) {
		if (bareSession == null) throw new VariantInternalException("Bare session cannot be null");
		this.bareSession = bareSession;
	}

	@Override
	public String getId() {
		return bareSession.getId();
	}

	@Override
	public long creationTimestamp() {
		return bareSession.creationTimestamp();
	}

	@Override
	public String getSchemaId() {
		return bareSession.getSchemaId();
	}

	@Override
	public Collection<Pair<State, Integer>> getTraversedStates() {
		return bareSession.getTraversedStates();
	}

	@Override
	public Collection<Test> getTraversedTests() {
		return bareSession.getTraversedTests();
	}

	@Override
	public Collection<Test> getDisqualifiedTests() {
		return bareSession.getDisqualifiedTests();
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		bareSession.triggerEvent(event);
	}

	@Override
	public boolean isExpired() {
		return bareSession.isExpired();
	}
	
	@Override
	public Object setAttribute(String name, Object value) {
		return bareSession.setAttribute(name, value);
	}

	@Override
	public Object getAttribute(String name) {
		return bareSession.getAttribute(name);
	}

	// ---------------------------------------------------------------------------------------------//
	//                                         PUBLIC EXT                                           //
	// ---------------------------------------------------------------------------------------------//

	public VariantSession getBareSession() {
		return bareSession;
	}

}
