package com.variant.client.conn;

import org.apache.http.HttpStatus;

import com.variant.client.Connection;
import com.variant.client.InternalErrorException;
import com.variant.client.Session;
import com.variant.client.impl.ClientError;
import com.variant.client.impl.ClientErrorException;
import com.variant.client.net.Payload;
import com.variant.client.net.http.HttpClient;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.VariantEvent;
import com.variant.core.impl.VariantEventSupport;
import com.variant.core.session.CoreSession;

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
	
	/**
	 * Connect this server to a schema.
	 * @return
	 */
	Payload.Connection connect() {
		
		if (isConnected) throw new InternalErrorException("Already connected");

		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.post(endpointUrl + "/connection/" + schemaName);
		isConnected = true;
		return Payload.Connection.fromResponse(resp);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void saveEvent(Session ssn, VariantEvent event) {
		
		checkState(ssn);
		
		// Remote
		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.post(endpointUrl + "event/", ((VariantEventSupport)event).toJson(ssn.getId()));
		
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
	public Payload.Session get(Connection conn, String sessionId) {

		checkState(conn);

		HttpClient httpClient = new HttpClient();
		HttpResponse resp = httpClient.get(endpointUrl + "session/" + sessionId);

		if (resp.status == HttpStatus.SC_OK) {
			return null;
			//return new SessionPayloadReader(conn, resp.body);
		}
		else if (resp.status == HttpStatus.SC_NO_CONTENT) {
			return null;
		}
		else {
			//throw new VariantHttpClientException(resp);
			return null;
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
		
		if (resp.status != HttpStatus.SC_OK) {
			//throw new VariantHttpClientException(resp);
		}
	}

}
