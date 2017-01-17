package com.variant.client.servlet.impl;

import com.typesafe.config.Config;
import com.variant.client.VariantClient;
import com.variant.client.servlet.VariantServletClient;
import com.variant.client.servlet.VariantServletConnection;

/**
 * The implementation of {@link VariantServletClient}.
 * 
 * @author Igor Urisman
 * @since 0.6
 */
public class ServletClientImpl implements VariantServletClient {

	private VariantClient bareClient;
	
	/**
	 * Wrap the bare session in a servlet session, but only once.
	 * We don't want to keep re-wrapping the same bare session.
	 *
	private VariantServletSession wrapBareSession(Session vareSsn) {
		
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
	*/
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	/**
	 */		
	public ServletClientImpl() {
		this.bareClient = VariantClient.Factory.getInstance();
	}

	@Override
	public Config getConfig() {
		return bareClient.getConfig();
	}

	@Override
	public VariantServletConnection getConnection(String url) {
		return new ServletConnectionImpl(this, bareClient.getConnection(url));
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 */
	public VariantClient getBareClient() {
		return bareClient;
	}

}
