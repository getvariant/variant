package com.variant.client.net;

import com.variant.client.Connection;
import com.variant.client.net.http.HttpClient;

/**
 * The abstraction of the remote server.
 * @author Igor
 */
public class Server {

	public Server() {
		
	}
	
	public ConnectionPayloadReader getConnection(String schema) {
		
	}

	/**
	 * GET or create session by ID.
	 * In 0.6 we're not able to create session on the server because the server
	 * does not understand schemas.
	 * 
	 * @since 0.6
	 */
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

}
