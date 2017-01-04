package com.variant.client.conn;

import org.apache.http.HttpStatus;

import com.variant.client.Connection;
import com.variant.client.InternalErrorException;
import com.variant.client.Session;
import com.variant.client.impl.ClientError;
import com.variant.client.impl.ClientErrorException;
import com.variant.client.net.Payload;
import com.variant.client.net.http.HttpAdapter;
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

	/**
	 * Bootstrapping 
	 * Package visibility as we only want ConnectionImpl to call this.
	 */
	Server(String url) {
		int lastColonIx = url.lastIndexOf(':');
		if (lastColonIx < 0) throw new ClientErrorException(ClientError.BAD_CONN_URL, url);
		String ep = url.substring(0, lastColonIx);
		endpointUrl = ep.endsWith("/") ? ep : ep + "/";
		schemaName = url.substring(lastColonIx + 1);
	}
	
	/**
	 * Connect this server to a schema.
	 * @return
	 */
	Payload.Connection connect() {
		
		if (isConnected) throw new InternalErrorException("Already connected");

		HttpAdapter httpAdapter = new HttpAdapter();
		HttpResponse resp = httpAdapter.post(endpointUrl + "connection/" + schemaName);
		isConnected = true;
		return Payload.Connection.fromResponse(resp);
	}
	
	/**
	 * Close this server connection.
	 */
	void disconnect(String id) {
		if (!isConnected) return;
		HttpAdapter httpClient = new HttpAdapter();
		httpClient.delete(endpointUrl + "connection/" + id);
		isConnected = true;		
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void saveEvent(Session ssn, VariantEvent event) {
		
		checkState(ssn);
		
		// Remote
		HttpAdapter httpClient = new HttpAdapter();
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

		HttpAdapter httpClient = new HttpAdapter();
		HttpResponse resp = httpClient.get(endpointUrl + "session/" + sessionId);

		if (resp.status == HttpStatus.SC_OK) {
			return Payload.Session.fromResponse(conn, resp);
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
		HttpAdapter httpClient = new HttpAdapter();
		httpClient.put(endpointUrl + "session/" + session.getId(), session.toJson());
		
	}

}
