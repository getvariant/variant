package com.variant.client.session;

import java.util.Map;

import org.apache.http.HttpStatus;

import com.variant.client.net.SessionPayloadReader;
import com.variant.client.net.http.HttpClient;
import com.variant.client.net.http.HttpResponse;
import com.variant.client.net.http.VariantHttpClientException;
import com.variant.core.impl.VariantCore;
import com.variant.core.session.CoreSession;
import com.variant.core.session.SessionStore;

public class SessionStoreImplRemote {

	//private static final Logger LOG = LoggerFactory.getLogger(SessionStoreRemote.class);
	
	private String apiEndpointUrl = null;

	/**
	 * GET or create session by ID.
	 * In 0.6 we're not able to create session on the server because the server
	 * does not understand schemas.
	 * 
	 * @since 0.6
	 *
	public SessionPayloadReader get(String sessionId, boolean create) {

		if (sessionId == null || sessionId.length() == 0) {
			throw new IllegalArgumentException("No session ID");
		}

		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(apiEndpointUrl + "session/" + sessionId);

		if (resp.getStatus() == HttpStatus.SC_OK) {
			return new SessionPayloadReader(coreApi, resp.getBody());
		}
		else if (resp.getStatus() == HttpStatus.SC_NO_CONTENT) {
			if (create) {
				CoreSession newSession = new CoreSession(sessionId, coreApi);
				save(newSession);
				return new SessionPayloadReader(coreApi, newSession.toJson());
			}
			else {
				return null;
			}
		}
		else {
			throw new VariantHttpClientException(resp);
		}
	}

	/**
	 * Save the session in the remote server.
	 */
	public void save(CoreSession session) {

		if (session == null) {
			throw new IllegalArgumentException("No session");
		}
		
		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.put(apiEndpointUrl + "session/" + session.getId(), ((CoreSession)session).toJson());
		
		if (resp.getStatus() != HttpStatus.SC_OK) {
			throw new VariantHttpClientException(resp);
		}
	}

	/**
	 * 
	 */
	@Override
	public void shutdown() {}

	/**
	 * 
	 */
	@Override
	public void init(VariantCore core, Map<String, Object> initObject) {
		coreApi = core;
		apiEndpointUrl = coreApi.getProperties().get(SERVER_ENDPOINT_URL);
		if (!apiEndpointUrl.endsWith("/")) apiEndpointUrl += "/";
	}
	
}
