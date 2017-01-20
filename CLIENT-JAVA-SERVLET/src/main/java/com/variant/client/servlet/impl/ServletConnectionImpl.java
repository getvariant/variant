package com.variant.client.servlet.impl;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.VariantClient;
import com.variant.client.servlet.ServletClientException;
import com.variant.client.servlet.VariantServletClient;
import com.variant.client.servlet.VariantServletConnection;
import com.variant.client.servlet.VariantServletSession;
import com.variant.core.schema.Schema;

public class ServletConnectionImpl implements VariantServletConnection {

	private static final String ATTR_NAME = "variant-wrap-session";
	private final VariantServletClient wrapClient;
	private final Connection bareConnection;
	
	/**
	 * Wrap the bare session, but only if we haven't already. We do this
	 * to preserve the bare API's idempotency of the getSession() calls, i.e.
	 * we don't want to re-wrap the same bare session into distinct wrapper
	 * sessions.
	 * 
	 * @param bareSsn
	 * @return
	 */
	private ServletSessionImpl wrap(Session bareSsn) {
		
		if (bareSsn == null) return null;
		
		// If this bare session has already been wrapped, don't re-wrap.
		ServletSessionImpl result = (ServletSessionImpl) bareSsn.getAttribute(ATTR_NAME);
		if (result == null) {
			// Not yet been wrapped.
			result = new ServletSessionImpl(this, bareSsn);
			bareSsn.setAttribute(ATTR_NAME, result);
		}
		return result;

	}
	
	/**
	 */
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
		if (userData.length != 1 || !(userData[0] instanceof HttpServletRequest)) 
			throw new ServletClientException("User data must have one element of type HttpServletRequest");
		return getOrCreateSession((HttpServletRequest) userData[0]);
	}

	public VariantServletSession getOrCreateSession(HttpServletRequest req) {
		return wrap(bareConnection.getOrCreateSession(req));
	}

	@Override
	public Schema getSchema() {
		return bareConnection.getSchema();
	}

	@Override
	public VariantServletSession getSession(Object... userData) {
		if (userData.length != 1 || !(userData[0] instanceof HttpServletRequest)) 
			throw new ServletClientException("User data must have one element of type HttpServletRequest");
		return getSession((HttpServletRequest) userData[0]);
	}

	public VariantServletSession getSession(HttpServletRequest req) {
		return wrap(bareConnection.getSession(req));
	}

	@Override
	public VariantServletSession getSessionById(String sid) {
		return wrap(bareConnection.getSessionById(sid));
	}

	@Override
	public Status getStatus() {
		return bareConnection.getStatus();
	}

	
}