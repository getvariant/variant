package com.variant.client.impl;

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.client.ClientException;
import com.variant.client.ConfigKeys;
import com.variant.client.Connection;
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

	private final String serverUrl;
	private final HttpAdapter adapter;

	/**
	 * All outbound operations which expect a return type.
	 *
	 * @param <T>
	 */
	private abstract class CommonExceptionHandler<T> {
		
		abstract T block() throws Exception;
		
		T run(Connection conn) {
			
			try { 
				return block(); 
			}
			// Intercept certain user exceptions.
			catch (ClientException.User ce) {
				if (ce.getError() == ServerError.UnknownConnection) {
					// The server has hung up on this connection.
					((ConnectionImpl)conn).setStatus(ConnectionStatus.CLOSED_BY_SERVER);
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
		
		final Object block() throws Exception {voidBlock(); return null;}
		abstract void voidBlock() throws Exception;
	}

	final VariantClientImpl client;
	
	/**
	 * Bootstrapping 
	 * Package visibility as we only want ConnectionImpl to call this.
	 */
	Server(VariantClientImpl client) {
		this.client = client;
		this.adapter = new HttpAdapter();
		String url = client.getConfig().getString(ConfigKeys.SERVER_URL);
		this.serverUrl =  url.endsWith("/") ? url : url + "/";
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                        /CONNECTION                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Connect this server to a schema.
	 * @return
	 */
	Payload.Connection connect(String schema) {
		
		if (LOG.isTraceEnabled()) LOG.trace("connect()");

		HttpResponse resp = adapter.post(serverUrl + "connection/" + schema);
		return Payload.Connection.fromResponse(resp);
	}
	
	/**
	 * Close this server connection (client side operation).
	 */
	void disconnect(ConnectionImpl conn) {
		
		if (LOG.isTraceEnabled()) LOG.trace("disconnect()");

		adapter.delete(serverUrl + "connection", conn);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void eventSave(final Session ssn, final VariantEvent event) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("eventSave(%s,%s)", ssn.getId(), event.getName()));

		new CommonExceptionHandlerVoid() {
			@Override void voidBlock() throws Exception {
				adapter.post(serverUrl + "event/", ((VariantEventSupport)event).toJson(), ssn.getConnection());
			}
		}.run(ssn.getConnection());
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          /SESSION                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * GET /session
	 * Get an existing session by ID or null if does not exist on the server.
	 */
	public Payload.Session sessionGet(final String sid, final ConnectionImpl conn) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionGet(%s)", sid));
		
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				try {
					HttpResponse resp = adapter.get(serverUrl + "session/" + sid, conn);
					return Payload.Session.fromResponse(conn, resp);
				}
				catch (ClientException.User ue) {
					// If the server is saying the session wasn't there, this method
					// should simply return null.
					if (ue.getError() == ServerError.SessionExpired) return null;
					else throw ue;
				}
			}
		}.run(conn);
	}

	/**
	 * Save or replace session on server.
	 * Is this actually needed?
	 */
	public void sessionSave(final Session ssn) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionSave(%s)", ssn.getId()));

		new CommonExceptionHandlerVoid() {
			@Override void voidBlock() throws Exception {
				String body = ((SessionImpl)ssn).getCoreSession().toJson();
				adapter.put(serverUrl + "session", body, ssn.getConnection());
			}
		}.run(ssn.getConnection());
		
	}

	/**
	 * Set a session attribute.
	 * @return previous global value of this attribute.
	 * 
	 */
	public String sessionAttrSet(Session ssn, String name, String value) {
		
		if (name == null) throw new NullPointerException("Name cannot be null");
		if (value == null) throw new NullPointerException("Value cannot be null");
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionAttrSet(%s, %s)", name, value));

		// Make body
		final StringWriter body = new StringWriter(2048);
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("sid", ssn.getId());
			jsonGen.writeStringField("name", name);
			jsonGen.writeStringField("value", value);
			jsonGen.writeEndObject();
		}
		catch (Exception t) {
			throw new ClientException.Internal(t);
		}
		
		// Call server
		Payload.Session response =
				new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.put(serverUrl + "session/attr", body.toString(), ssn.getConnection());
				return Payload.Session.fromResponse(ssn.getConnection(), resp);
			}
		}.run(ssn.getConnection());
		
		return response.returns;
	}

	//---------------------------------------------------------------------------------------------//
	//                                         /REQUEST                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * POST /request.
     * Create a state request by targeting session for a state
	 */
	public Payload.Session requestCreate(String sid, String state, final ConnectionImpl conn) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCreate(%s,%s)", sid, state));

		final String body = String.format("{\"sid\":\"%s\",\"state\":\"%s\"}", sid, state);
		
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.post(serverUrl + "request", body, conn); 
				return Payload.Session.fromResponse(conn, resp);
			}
		}.run(conn);
	}

	/**
	 * PUT /request.
	 * Commit a state request and trigger the state visited event.
	 */
	public Payload.Session requestCommit(final CoreSession ssn, final ConnectionImpl conn) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCommit(%s)", ssn.getId()));

		return new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
				StringWriter body = new StringWriter();
				JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
				jsonGen.writeStartObject();
				jsonGen.writeStringField("sid", ssn.getId());
				//jsonGen.writeStringField("cid", connection.getId());
				//jsonGen.writeStringField("ssn", ssn.toJson());
				jsonGen.writeEndObject();
				jsonGen.flush();
				HttpResponse resp = adapter.put(serverUrl + "request", body.toString(), conn);
				return Payload.Session.fromResponse(conn, resp);
			}
		}.run(conn);

	}

}
