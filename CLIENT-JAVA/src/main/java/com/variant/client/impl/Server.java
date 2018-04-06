package com.variant.client.impl;

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.client.ClientException;
import com.variant.client.ConfigKeys;
import com.variant.core.ConnectionStatus;
import com.variant.client.ConnectionClosedException;
import com.variant.client.Session;
import com.variant.client.net.Payload;
import com.variant.client.net.http.HttpAdapter;
import com.variant.client.net.http.HttpRemoter;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.ServerError;
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

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	private final ConnectionImpl connection;
	private final String schemaName;
	private final String serverUrl;
	private final HttpRemoter remoter;
	private final HttpAdapter adapter;
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
	 * All outbound operations which expect a return type.
	 *
	 * @param <T>
	 */
	private abstract class CommonExceptionHandler<T> {
		
		abstract T code() throws Exception;
		
		T run() {
			
			try { 
				return code(); 
			}
			// Intercept certain user exceptions.
			catch (ClientException.User ce) {
				if (ce.getError() == ServerError.UnknownConnection) {
					// The server has hung up on this connection.
					destroy();
					connection.close(ConnectionStatus.CLOSED_BY_SERVER);
					throw new ConnectionClosedException(ce);
				}
				else throw ce;
			}

			// Pass through internal exceptions
			catch (ClientException.Internal ce) { 
				throw ce;
			}

			// Something unexpected - wrap as an Internal.
			catch (Exception e) {
				throw new ClientException.Internal("Unexpected Exception", e);
			}
		}
	}

	/**
	 * All outbound operations which do not expect a return type.
	 *
	 * @param <T>
	 */
	private abstract class CommonExceptionHandlerVoid extends CommonExceptionHandler<Object> {
		
		final Object code() throws Exception {codeVoid(); return null;}
		abstract void codeVoid() throws Exception;
	}

	/**
	 * Destroy this server.
	 */
	private void destroy() {
		if (remoter != null) {
			remoter.destroy();
		}
		isConnected = false;
	}
	
	/**
	 * Bootstrapping 
	 * Package visibility as we only want ConnectionImpl to call this.
	 */
	Server(ConnectionImpl connection, String schemaName) {
		this.connection = connection;
		this.remoter = new HttpRemoter(connection);
		this.adapter = new HttpAdapter(remoter);
		String url = connection.getClient().getConfig().getString(ConfigKeys.SERVER_URL);
		this.serverUrl =  url.endsWith("/") ? url : url + "/";
		this.schemaName = schemaName;
	}
	
	/**
	 * Connect this server to a schema.
	 * @return
	 */
	Payload.Connection connect() {
		
		if (LOG.isTraceEnabled()) LOG.trace("connect()");

		if (isConnected) throw new ClientException.Internal("Already connected");

		HttpResponse resp = adapter.post(serverUrl + "connection/" + schemaName);
		isConnected = true;
		return Payload.Connection.fromResponse(resp);
	}
	
	/**
	 * Close this server connection.
	 */
	void disconnect(String id) {
		
		if (LOG.isTraceEnabled()) LOG.trace("disconnect()");

		if (isConnected) {
			adapter.delete(serverUrl + "connection");
			destroy();
		}
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void eventSave(Session ssn, VariantEvent event) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("eventSave(%s,%s)", ssn.getId(), event.getName()));

		checkState(ssn);
		
		// Remote
		adapter.post(serverUrl + "event/", ((VariantEventSupport)event).toJson());
		
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          /SESSION                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * GET /session
	 * Get an existing session by ID or null if does not exist on the server.
	 */
	public Payload.Session sessionGet(final String sid) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionGet(%s)", sid));

		checkState();
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session code() throws Exception {
				try {
					HttpResponse resp = adapter.get(serverUrl + "session/" + sid);
					return Payload.Session.fromResponse(connection, resp);
				}
				catch (ClientException.User ue) {
					// If the server is saying the session wasn't there, this method
					// should simply return null.
					if (ue.getError() == ServerError.SessionExpired) return null;
					else throw ue;
				}
			}
		}.run();
	}

	/**
	 * Save or replace session on server.
	 */
	public void sessionSave(final Session ssn) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionSave(%s)", ssn.getId()));

		checkState(ssn);

		new CommonExceptionHandlerVoid() {
			@Override void codeVoid() throws Exception {
				String body = ((SessionImpl)ssn).getCoreSession().toJson();
				adapter.put(serverUrl + "session", body);
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

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCreate(%s,%s)", sid, state));

		checkState();

		final String body = String.format("{\"sid\":\"%s\",\"state\":\"%s\"}", sid, state);
		
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session code() throws Exception {
				HttpResponse resp = adapter.post(serverUrl + "request", body); 
				return Payload.Session.fromResponse(connection, resp);
			}
		}.run();
	}

	/**
	 * PUT /request.
	 * Commit a state request and trigger the state visited event.
	 */
	public Payload.Session requestCommit(final CoreSession ssn) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCommit(%s)", ssn.getId()));

		checkState();

		return new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session code() throws Exception {
				StringWriter body = new StringWriter();
				JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
				jsonGen.writeStartObject();
				jsonGen.writeStringField("sid", ssn.getId());
				//jsonGen.writeStringField("cid", connection.getId());
				//jsonGen.writeStringField("ssn", ssn.toJson());
				jsonGen.writeEndObject();
				jsonGen.flush();
				HttpResponse resp = adapter.put(serverUrl + "request", body.toString());
				return Payload.Session.fromResponse(connection, resp);
			}
		}.run();

	}

}
