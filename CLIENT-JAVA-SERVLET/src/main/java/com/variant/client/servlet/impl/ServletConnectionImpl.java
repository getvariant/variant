package com.variant.client.servlet.impl;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.servlet.VariantServletClient;
import com.variant.client.servlet.VariantServletConnection;
import com.variant.client.servlet.VariantServletSession;
import com.variant.core.schema.Schema;

public class ServletConnectionImpl implements VariantServletConnection {

	private final VariantServletClient wrapClient;
	private final Connection bareConnection;
	
	public ServletConnectionImpl(VariantServletClient wrapClient, Connection bareConnection) {
		this.wrapClient = wrapClient;
		this.bareConnection = bareConnection;
	}
	
	
	@Override
	public void close() {
		bareConnection.close();
	}

	@Override
	public VariantClient getClient() {
		return wrapClient;
	}

	@Override
	public VariantServletSession getOrCreateSession(Object... userData) {
		return getOrCreateSession((HttpServletRequest) userData[0]);
	}

	public VariantServletSession getOrCreateSession(HttpServletRequest req) {
		return new ServletSessionImpl(this, bareConnection.getOrCreateSession(req));
	}

	@Override
	public Schema getSchema() {
		return bareConnection.getSchema();
	}

	@Override
	public VariantServletSession getSession(Object... userData) {
		return getSession((HttpServletRequest) userData[0]);
	}

	public VariantServletSession getSession(HttpServletRequest req) {
		Session bareSession = bareConnection.getSession(req);
		return bareSession == null ? null : new ServletSessionImpl(this, bareSession);
	}

	@Override
	public VariantServletSession getSessionById(String sid) {
		Session bareSession = bareConnection.getSessionById(sid);
		return bareSession == null ? null : new ServletSessionImpl(this, bareSession);
	}

	@Override
	public Status getStatus() {
		return bareConnection.getStatus();
	}

	
}
