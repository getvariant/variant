package com.variant.client.conn;

import com.variant.client.ClientException;
import com.variant.client.Connection.Status;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.impl.ClientUserError;
import com.variant.client.net.Payload;
import com.variant.client.net.http.HttpAdapter;
import com.variant.client.net.http.HttpRemoter;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.VariantEvent;
import com.variant.core.exception.ServerError;
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
	private HttpRemoter remoter = new HttpRemoter();
	private final HttpAdapter adapter = new HttpAdapter(remoter);
	private ConnectionImpl connection;
	private boolean isConnected = false;
	
	/**
	 * Consistency checks.
	 * @param conn
	 */
	private void checkState() {
		
	}
	
	private void checkState(Session ssn) {
		
	}

	/**
	 * SCID
	 */
	private String scid(String sid) {
		return sid + "." + connection.getId();
	}
	
	/**
	 * Destroy this server.
	 */
	private void destroy() {
		if (remoter != null) {
			remoter.destroy();
			remoter = null;
		}
		isConnected = false;
	}
	
	/**
	 * Bootstrapping 
	 * Package visibility as we only want ConnectionImpl to call this.
	 */
	Server(ConnectionImpl connection, String url) {
		this.connection = connection;
		int lastColonIx = url.lastIndexOf(':');
		if (lastColonIx < 0) throw new ClientException.User(ClientUserError.BAD_CONN_URL, url);
		String ep = url.substring(0, lastColonIx);
		endpointUrl = ep.endsWith("/") ? ep : ep + "/";
		schemaName = url.substring(lastColonIx + 1);
	}
	
	/**
	 * Connect this server to a schema.
	 * @return
	 */
	Payload.Connection connect() {
		
		if (isConnected) throw new ClientException.Internal("Already connected");

		HttpResponse resp = adapter.post(endpointUrl + "connection/" + schemaName);
		isConnected = true;
		return Payload.Connection.fromResponse(resp);
	}
	
	/**
	 * Close this server connection.
	 */
	void disconnect(String id) {
		if (isConnected) {
			adapter.delete(endpointUrl + "connection/" + id);
			destroy();
		}
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void saveEvent(Session ssn, VariantEvent event) {
		
		checkState(ssn);
		
		// Remote
		adapter.post(endpointUrl + "event/", ((VariantEventSupport)event).toJson());
		
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          /SESSION                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * GET /session
	 * Get or create session by ID.
	 */
	public Payload.Session get(String sid) {

		checkState();

		HttpResponse resp = adapter.get(endpointUrl + "session/" + scid(sid));
		return Payload.Session.fromResponse(connection, resp);
	}

	/**
	 * Save core session on the remote server.
	 */
	public void saveSession(CoreSession session) {
		
		checkState();

		// Remote
		try {
			adapter.put(endpointUrl + "session/" + scid(session.getId()), session.toJson());
		}
		catch (ClientException.User ce) {
			if (ce.getError() == ServerError.UnknownConnection) {
				// The server has hung up on this connection.
				destroy();
				connection.close(Status.CLOSED_BY_SERVER);
				throw new ConnectionClosedException(ce);
			}
		}
	}

	//---------------------------------------------------------------------------------------------//
	//                                          /TARGET                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * POST /target.
     * Target session for a state
	 */
	public Payload.Session target(String sid, String state) {

		checkState();

		String body = String.format("{\"sid\":\"%s\",\"state\":\"%s\"}", scid(sid), state);
		
		HttpResponse resp = adapter.post(endpointUrl + "target", body); 
		return Payload.Session.fromResponse(connection, resp);
	}

}
