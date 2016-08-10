package com.variant.client.servlet.impl;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.servlet.VariantServletClient;
import com.variant.client.servlet.VariantServletSession;
import com.variant.core.VariantProperties;
import com.variant.core.event.impl.util.VariantArrayUtils;
import com.variant.core.hook.HookListener;
import com.variant.core.schema.Schema;
import com.variant.core.schema.parser.ParserResponse;

/**
 * The implementation of {@link VariantServletClient}.
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class ServletClientImpl implements VariantServletClient {

	private static final String WRAP_ATTR_NAME = ServletClientImpl.class.getSimpleName();
	private VariantClient client;
	
	/**
	 * Wrap the bare session in a servlet session, but only once.
	 * We don't want to keep re-wrapping the same bare session.
	 */
	private VariantServletSession wrapBareSession(VariantSession bareSession) {
		
		if (bareSession == null) return null;
		
		// If this bare session has already been wrapped, don't re-wrap.
		VariantServletSession result = (VariantServletSession) bareSession.getAttribute(WRAP_ATTR_NAME);
		if (result == null) {
			// Not yet been wrapped.
			result = new ServletSessionImpl(bareSession);
			bareSession.setAttribute(WRAP_ATTR_NAME, result);
		}
		return result;
	}
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */		
	public ServletClientImpl(String...resourceNames) {
		String[] newArgs = VariantArrayUtils.prepend("/com/variant/client/conf/servlet-adapter.props", resourceNames, String.class);
		this.client = VariantClient.Factory.getInstance(newArgs);
	}

	/**
	 */
	@Override
	public VariantProperties getProperties() {
		return client.getProperties();
	}

	/**
	 */
	@Override
	public void addHookListener(HookListener<?> listener) {
		client.addHookListener(listener);
	}
	
	/**
	 */
	@Override
	public void clearHookListeners() {
		client.clearHookListeners();
	}

	/**
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream, boolean deploy) {
		return client.parseSchema(stream, deploy);
	}

	/**
	 */
	@Override
	public ParserResponse parseSchema(InputStream stream) {
		return client.parseSchema(stream);
	}

	/**
	 */
	@Override
	public Schema getSchema() {
		return client.getSchema();
	}

	/**
	 */
	@Override
	public VariantServletSession getOrCreateSession(Object... userData) {
		return wrapBareSession(client.getOrCreateSession(userData));
	}

	/**
	 */
	@Override
	public VariantServletSession getSession(Object...userData) {
		return wrapBareSession(client.getSession(userData));
	}

	/**
	 */
	@Override
	public VariantServletSession getOrCreateSession(HttpServletRequest request) {
		return wrapBareSession(client.getOrCreateSession(request));
	}

	/**
	 */
	@Override
	public VariantServletSession getSession(HttpServletRequest request) {
		return wrapBareSession(client.getSession(request));
	}


	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClient getBareClient() {
		return client;
	}

}
