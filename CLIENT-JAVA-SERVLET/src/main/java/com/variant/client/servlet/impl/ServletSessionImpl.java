package com.variant.client.servlet.impl;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.variant.client.ClientException;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.StateRequest;
import com.variant.client.servlet.VariantServletSession;
import com.variant.client.servlet.VariantServletStateRequest;
import com.variant.core.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * <p>The implementation of {@link VariantServletSession}.
 * Replaces bare client's {@link VariantCoreSession#targetForState(com.variant.core.xdm.State)} 
 * method with one, which returns the servlet-aware implementation of {@link VariantCoreStateRequest}. 
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class ServletSessionImpl implements VariantServletSession {

	private final Connection wrapConnection;
	private final Session bareSession;
	private VariantServletStateRequest wrapStateRequest;

	/**
	 * 
	 */
	public ServletSessionImpl(ServletConnectionImpl wrapConnection, Session bareSession) {
		if (wrapConnection == null) throw new ClientException.Internal("Servlet connection cannot be null");
		if (bareSession == null) throw new ClientException.Internal("Bare session cannot be null");
		this.wrapConnection = wrapConnection;
		this.bareSession = bareSession;
	}

	@Override
	public VariantServletStateRequest targetForState(State state) {
		wrapStateRequest = new ServletStateRequestImpl(this, bareSession.targetForState(state));
		return wrapStateRequest;
	}

	@Override
	public String clearAttribute(String name) {
		return bareSession.clearAttribute(name);
	}

	@Override
	public String getAttribute(String name) {
		return bareSession.getAttribute(name);
	}

	@Override
	public Connection getConnection() {
		return wrapConnection;
	}

	@Override
	public Date getCreateDate() {
		return bareSession.getCreateDate();
	}

	@Override
	public Set<Test> getDisqualifiedTests() {
		return bareSession.getDisqualifiedTests();
	}

	@Override
	public String getId() {
		return bareSession.getId();
	}

	@Override
	public StateRequest getStateRequest() {
		return wrapStateRequest;
	}

	@Override
	public long getTimeoutMillis() {
		return bareSession.getTimeoutMillis();
	}

	@Override
	public Map<State, Integer> getTraversedStates() {
		return bareSession.getTraversedStates();
	}

	@Override
	public Set<Test> getTraversedTests() {
		return bareSession.getTraversedTests();
	}

	@Override
	public boolean isExpired() {
		return bareSession.isExpired();
	}

	@Override
	public String setAttribute(String name, String value) {
		return bareSession.setAttribute(name, value);
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		bareSession.triggerEvent(event);
	}


}
