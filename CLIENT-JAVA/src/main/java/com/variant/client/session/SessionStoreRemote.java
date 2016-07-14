package com.variant.client.session;

import static com.variant.client.VariantClientPropertyKeys.SERVER_ENDPOINT_URL;

import java.util.Map;

import org.apache.http.HttpStatus;

import com.variant.client.http.HttpClient;
import com.variant.client.http.HttpResponse;
import com.variant.client.http.VariantHttpClientException;
import com.variant.core.VariantCoreSession;
import com.variant.core.impl.CoreSessionImpl;
import com.variant.core.impl.VariantCore;
import com.variant.core.session.SessionStore;

public class SessionStoreRemote implements SessionStore {

	//private static final Logger LOG = LoggerFactory.getLogger(SessionStoreRemote.class);
	
	private String apiEndpointUrl = null;
	private VariantCore coreApi = null;

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
