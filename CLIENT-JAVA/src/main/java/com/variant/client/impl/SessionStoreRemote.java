package com.variant.client.impl;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.variant.client.http.HttpClient;
import com.variant.client.http.HttpResponse;
import com.variant.client.http.VariantHttpClientException;
import com.variant.core.VariantCoreInitParams;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.impl.CorePropertiesImpl;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantComptime;
import com.variant.core.impl.VariantCore;
import com.variant.core.impl.VariantCoreInitParamsImpl;

public class SessionStoreRemote implements VariantSessionStore {

	private static final Logger LOG = LoggerFactory.getLogger(SessionStoreRemote.class);
	
	private String apiEndpointUrl = null;
	private VariantCore coreApi;
	private long sessionTimeoutMillis;  // dont need on client.

	/**
	 * GET or create session by ID.
	 * In 0.6 we're not able to create session on the server because the server
	 * does not understand schemas. To avoid an extra trip to the server, we'll
	 * pre-create a blank session and send it in the body of the GET request,
	 * 
	 * @since 0.6
	 */
	@Override
	public VariantCoreSession get(String sessionId) {

		if (sessionId == null || sessionId.length() == 0) {
			throw new IllegalArgumentException("No session ID");
		}
						
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(apiEndpointUrl + "session/" + sessionId);

		if (resp.getStatus() == HttpStatus.SC_OK) {
			return CoreSessionImpl.fromJson(coreApi, resp.getBody());
		}
		else if (resp.getStatus() == HttpStatus.SC_NO_CONTENT) {
			return null;
		}
		else {
			throw new VariantHttpClientException(resp);
		}
	}

	/**
	 * Save the session in the remote server.
	 */
	@Override
	public void save(VariantCoreSession session) {

		if (session == null) {
			throw new IllegalArgumentException("No session");
		}
		
		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.put(apiEndpointUrl + "session/" + session.getId(), ((CoreSessionImpl)session).toJson());
		
		if (resp.getStatus() != HttpStatus.SC_OK) {
			throw new VariantHttpClientException(resp);
		}
	}

	/**
	 * 
	 */
	@Override
	public void initialized(VariantCoreInitParams initParams) throws Exception {

		apiEndpointUrl = (String) initParams.getOrThrow(
				"apiEndpoint", 
				new VariantRuntimeException(RUN_PROPERTY_INIT_PROPERTY_NOT_SET, "url", this.getClass().getName(), CorePropertiesImpl.Key.EVENT_PERSISTER_CLASS_INIT.propName()));
		
		sessionTimeoutMillis = (Integer) initParams.getOr("sessionTimeoutSecs",  "900") * 1000;
		
		coreApi = ((VariantCoreInitParamsImpl)initParams).getCoreApi();
		
		if (coreApi.getComptime().getComponent() != VariantComptime.Component.SERVER) {
			apiEndpointUrl = coreApi.getProperties().get(CorePropertiesImpl.Key.SERVER_ENDPOINT_URL);
		}
		if (!apiEndpointUrl.endsWith("/")) apiEndpointUrl += "/";
	}

	@Override
	public void shutdown() {}
	
}
