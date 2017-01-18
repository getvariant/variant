package com.variant.client.conn;

import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.client.ClientException;
import com.variant.client.ConfigKeys;
import com.variant.client.Connection.Status;
import com.variant.client.ClientUserError;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.impl.SessionImpl;
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

	private final String serverUrl;
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

	private abstract class CommonExceptionHandler<T> {
		
		abstract T code() throws Exception;
		
		T run() {
			try { return code(); }
			
			// Already properly packaged exception.
			catch (ClientException ce) { 
				throw ce;
			}
			// Something unexpected.
			catch (Exception e) {
				throw new ClientException.Internal("Unexpected Exception", e);
			}
		}
	}

	private abstract class CommonExceptionHandlerVoid extends CommonExceptionHandler<Object> {
		
		final Object code() throws Exception {codeVoid(); return null;}
		abstract void codeVoid() throws Exception;
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
	Server(ConnectionImpl connection, String schemaName) {
		this.connection = connection;
		String url = connection.getClient().getConfig().getString(ConfigKeys.SERVER_URL);
		this.serverUrl =  url.endsWith("/") ? url : url + "/";
		this.schemaName = schemaName;
	}
	
	/**
	 * Connect this server to a schema.
	 * @return
	 */
	Payload.Connection connect() {
		
		if (isConnected) throw new ClientException.Internal("Already connected");

		HttpResponse resp = adapter.post(serverUrl + "connection/" + schemaName);
		isConnected = true;
		return Payload.Connection.fromResponse(resp);
	}
	
	/**
	 * Close this server connection.
	 */
	void disconnect(String id) {
		if (isConnected) {
			adapter.delete(serverUrl + "connection/" + id);
			destroy();
		}
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void eventSave(Session ssn, VariantEvent event) {
		
		checkState(ssn);
		
		// Remote
		adapter.post(serverUrl + "event/", ((VariantEventSupport)event).toJson());
		
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          /SESSION                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * GET /session
	 * Get or create session by ID.
	 */
	public Payload.Session sessionGet(final String sid) {

		checkState();
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session code() throws Exception {
				HttpResponse resp = adapter.get(serverUrl + "session/" + scid(sid));
				return Payload.Session.fromResponse(connection, resp);
			}
		}.run();
	}

	/**
	 * Save core session on the remote server.
	 */
	public void sessionSave(final Session session) {
		
		checkState(session);

		new CommonExceptionHandlerVoid() {
			
			@Override void codeVoid() throws Exception {
				try {
					StringWriter body = new StringWriter();
					JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
					jsonGen.writeStartObject();
					jsonGen.writeStringField("cid", connection.getId());
					jsonGen.writeStringField("ssn", ((SessionImpl)session).getCoreSession().toJson());
					jsonGen.writeEndObject();
					jsonGen.flush();
					adapter.put(serverUrl + "session", body.toString());
				}
				catch (ClientException.User ce) {
					if (ce.getError() == ServerError.UnknownConnection) {
						// The server has hung up on this connection.
						destroy();
						connection.close(Status.CLOSED_BY_SERVER);
						throw new ConnectionClosedException(ce);
					}
					else throw ce;
				}
			}
		}.run();
		
	}

	//---------------------------------------------------------------------------------------------//
	//                                         /REQUEST                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * POST /request.
     * Create a state request by targeting session for a state
	 */
	public Payload.Session requestCreate(String sid, String state) {

		checkState();

		final String body = String.format("{\"sid\":\"%s\",\"state\":\"%s\"}", scid(sid), state);
		
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session code() throws Exception {
				HttpResponse resp = adapter.post(serverUrl + "request", body); 
				return Payload.Session.fromResponse(connection, resp);
			}
		}.run();
	}

	/**
	 * DELETE /request.
	 * Commit a state request and trigger the state visited event.
	 */
	public void requestCommit(final CoreSession session) {
		
		checkState();

		new CommonExceptionHandlerVoid() {
			
			@Override void codeVoid() throws Exception {
				try {
					adapter.put(serverUrl + "request" + scid(session.getId()), session.toJson());
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
		}.run();
	}

}
