package com.variant.client.net;

import org.apache.http.HttpStatus;

import static com.variant.client.ConfigKeys.*;
import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.VariantSession;
import com.variant.client.net.http.HttpClient;
import com.variant.client.net.http.HttpResponse;
import com.variant.client.net.http.VariantHttpClientException;
import com.variant.core.VariantEvent;
import com.variant.core.impl.VariantEventSupport;
import com.variant.core.session.CoreSession;

/**
 * The abstraction of the remote server.
 * @author Igor
 */
public class Server {

	private final VariantClient client;
	private final String endpointUrl;
	
	/**
	 * Consistency checks.
	 * @param conn
	 */
	private void checkState(Connection conn) {
		
	}
	
	private void checkState(VariantSession ssn) {
		
	}

	/**
	 * One Server per client.
	 * @param client
	 */
	public Server(VariantClient client) {
		this.client = client;
		String ep = client.getConfig().getString(SERVER_ENDPOINT_URL);
		endpointUrl = !ep.endsWith("/") ? ep : ep + "/";
	}
	
	public ConnectionPayloadReader getConnection(String schema) {
		throw new RuntimeException("Unsupported");
	}

	//---------------------------------------------------------------------------------------------//
	//                                        /CONNECTION                                          //
	//---------------------------------------------------------------------------------------------//

	public ConnectionPayloadReader getConnection() {
		return null;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void saveEvent(VariantSession ssn, VariantEvent event) {
		
		checkState(ssn);
		
		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.post(endpointUrl + "event/", ((VariantEventSupport)event).toJson(ssn.getId()));
		
		if (resp.getStatus() != HttpStatus.SC_OK) {
			throw new VariantHttpClientException(resp);
		}		
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          /SESSION                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * GET or create session by ID.
	 * In 0.6 we're not able to create session on the server because the server
	 * does not understand schemas.
	 * 
	 * @since 0.6
	 */
	public SessionPayloadReader get(Connection conn, String sessionId) {

		checkState(conn);

		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(endpointUrl + "session/" + sessionId);

		if (resp.getStatus() == HttpStatus.SC_OK) {
			return new SessionPayloadReader(conn, resp.getBody());
		}
		else if (resp.getStatus() == HttpStatus.SC_NO_CONTENT) {
			return null;
		}
		else {
			throw new VariantHttpClientException(resp);
		}
	}

	/**
	 * Save core session on the remote server.
	 */
	public void saveSession(Connection conn, CoreSession session) {
		
		checkState(conn);

		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.put(endpointUrl + "session/" + session.getId(), session.toJson(conn.getSchema()));
		
		if (resp.getStatus() != HttpStatus.SC_OK) {
			throw new VariantHttpClientException(resp);
		}
	}

}
