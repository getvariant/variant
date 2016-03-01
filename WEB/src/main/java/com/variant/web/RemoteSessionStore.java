package com.variant.web;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import com.variant.core.InitializationParams;
import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionIdTracker;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.session.VariantSessionImpl;
import com.variant.web.http.HttpClient;
import com.variant.web.http.HttpResponse;
import com.variant.webnative.SessionIdTrackerHttpCookie;

public class RemoteSessionStore implements VariantSessionStore {

	private String apiEndpointUrl = null;
	private Variant coreApi;

	/**
	 * We expect user data to be a single elem array and contain the session ID.
	 */
	@Override
	public VariantSession get(Object... userData) {
		if (userData == null || userData.length != 1 || !(userData[0] instanceof String)) {
			throw new VariantInternalException("Expected a single-element String array");
		}
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(apiEndpointUrl + "session/" + userData[0]);
		return VariantSessionImpl.fromJson(coreApi, resp.getBody());
	}


	/**
	 * We expect user data to be a single elem array and contain the session ID.
	 */
	@Override
	public void save(VariantSession session, Object... userData) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public VariantSessionIdTracker getSessionIdTracker() {
		return new SessionIdTrackerHttpCookie();
	}

	@Override
	public void initialized(InitializationParams initParams) throws Exception {
		apiEndpointUrl = initParams.getOrThrow(
				"apiEndpoint", 
				new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName(), VariantProperties.Key.EVENT_PERSISTER_CLASS_INIT.propName()));
		coreApi = initParams.getCoreApi();
	}

	@Override
	public void shutdown() {}
	
}
