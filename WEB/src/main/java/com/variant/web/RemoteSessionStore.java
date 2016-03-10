package com.variant.web;

import static com.variant.core.schema.impl.MessageTemplate.RUN_PROPERTY_INIT_PROPERTY_NOT_SET;

import org.apache.http.HttpStatus;

import com.variant.core.InitializationParams;
import com.variant.core.Variant;
import com.variant.core.VariantProperties;
import com.variant.core.VariantSession;
import com.variant.core.VariantSessionStore;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.session.VariantSessionImpl;
import com.variant.web.http.HttpClient;
import com.variant.web.http.HttpResponse;
import com.variant.web.http.VariantHttpClientException;

public class RemoteSessionStore implements VariantSessionStore {

	private String apiEndpointUrl = null;
	private Variant coreApi;

	/**
	 * GET or create session by ID.
	 * In 0.6 we're not able to create session on the server because the server
	 * does not understand schemas. To avoid an extra trip to the server, we'll
	 * pre-create a blank session and send it in the body of the GET request,
	 * 
	 * We don't care what's in userData as all we need is the session ID.
	 * @since 0.6
	 */
	@Override
	public VariantSession get(String sessionId, Object... userData) {

		if (sessionId == null || sessionId.length() == 0) {
			throw new IllegalArgumentException("No session ID");
		}
		
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(apiEndpointUrl + "session/" + sessionId);
		VariantSession result = null;

		if (resp.getStatus() == HttpStatus.SC_OK) {
			result = VariantSessionImpl.fromJson(coreApi, resp.getBody());
		}
		else if (resp.getStatus() == HttpStatus.SC_NO_CONTENT) {
			result = new VariantSessionImpl(coreApi, sessionId);
			save(result);
		}
		else {
			throw new VariantHttpClientException(resp);
		}
		return result;
	}

	/**
	 * Save the session in the repomote server.
	 */
	@Override
	public void save(VariantSession session, Object... userData) {

		if (session == null) {
			throw new IllegalArgumentException("No session");
		}

		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.put(apiEndpointUrl + "session/" + session.getId(), ((VariantSessionImpl)session).toJson());
		
		if (resp.getStatus() != HttpStatus.SC_OK) {
			throw new VariantHttpClientException(resp);
		}
	}

	/**
	 * 
	 */
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
