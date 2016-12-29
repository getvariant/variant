package com.variant.client.conn;

import org.apache.http.HttpStatus;

import static com.variant.client.ConfigKeys.*;

import com.variant.client.Connection;
import com.variant.client.VariantClient;
import com.variant.client.Session;
import com.variant.client.impl.ClientError;
import com.variant.client.impl.ClientErrorException;
import com.variant.client.net.ConnectionPayloadReader;
import com.variant.client.net.SessionPayloadReader;
import com.variant.client.net.http.HttpClient;
import com.variant.client.net.http.HttpResponse;
import com.variant.client.net.http.VariantHttpClientException;
import com.variant.core.VariantEvent;
import com.variant.core.exception.InternalException;
import com.variant.core.impl.VariantEventSupport;
import com.variant.core.session.CoreSession;
import com.variant.core.util.Tuples.Pair;

/**
 * The abstraction of the remote server.  
 * One server per connection, one connection per server.
 * 
 * @author Igor
 */
public class Server {

	private final String endpointUrl;
	private final String schemaName;
	
	private boolean isConnected = false;
	
	/**
	 * Consistency checks.
	 * @param conn
	 */
	private void checkState(Connection conn) {
		
	}
	
	private void checkState(Session ssn) {
		
	}

	//
	// Bootstrapping 
	// Package visibility as we only want ConnectionImpl to call this.
	//

	/**
	 * 
	 */
	Server(String url) {
		int lastColonIx = url.lastIndexOf(':');
		if (lastColonIx < 0) throw new ClientErrorException(ClientError.BAD_CONN_URL, url);
		endpointUrl = url.substring(0, lastColonIx);
		schemaName = url.substring(lastColonIx + 1);
	}
	
	ConnectionPayloadReader connect() {
		
		if (isConnected) throw new InternalException("Already connected");

		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.post(endpointUrl + "/connection/" + schemaName);
		
		if (resp.getStatus() != HttpStatus.SC_OK) {
			throw new VariantHttpClientException(resp);
		}		

		isConnected = true;
		return new ConnectionPayloadReader(resp.getBody());
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void saveEvent(Session ssn, VariantEvent event) {
		
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
