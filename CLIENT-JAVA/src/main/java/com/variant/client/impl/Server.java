package com.variant.client.impl;

import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.client.ClientException;
import com.variant.client.ConfigKeys;
import com.variant.client.Connection;
import com.variant.client.Session;
import com.variant.client.SessionExpiredException;
import com.variant.client.net.Payload;
import com.variant.client.net.http.HttpAdapter;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.VariantEvent;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.VariantEventSupport;

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
		
		/**
		 * All sessions should use this.
		 * All real work happens in {@link #run(Connection)}.
		 * Here we only intercept and clean out expired sessions.
		 */
		T run(Session ssn) {
			return run(ssn.getId(), ssn.getConnection());
		}

	   /**
		 * 
		 */
		T run(String sid, Connection conn) {
			
			try { 
				return block(); 
			}
			// Intercept certain user exceptions.
			catch (ClientException.User ce) {
				if (ce.getError() == ServerError.SessionExpired) {
					((ConnectionImpl)conn).cache.expire(sid);
					throw new SessionExpiredException(ce);
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
		return Payload.Connection.parse(resp);
	}
	
	/**
	 * Close this server connection (client side operation).
	 *
	void disconnect(ConnectionImpl conn) {
		
		if (LOG.isTraceEnabled()) LOG.trace("disconnect()");

		adapter.delete(serverUrl + "connection", conn);
	}
	*/
		
	//---------------------------------------------------------------------------------------------//
	//                                          /SESSION                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * GET /session/:schema/:sid
	 * Get an existing session by ID or null if does not exist on the server.
	 */
	public Payload.Session sessionGet(final String sid, final ConnectionImpl conn) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionGet(%s)", sid));
		
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.get(serverUrl + "session/" + conn.schema + "/" + sid);
				return Payload.Session.parse(conn, resp);
			}
		}.run(sid, conn);
	}

	/**
	 * POST /session/:schema/:sid
	 * Get or create an existing session by ID or null if does not exist on the server.
	 * Same URL as GET, no body.
	 */
	public Payload.Session sessionGetorCreate(final String sid, final ConnectionImpl conn) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionGet(%s)", sid));
		
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.post(serverUrl + "session/" + conn.schema + "/" + sid);
				return Payload.Session.parse(conn, resp);
			}
		}.run(sid, conn);
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
				adapter.put(serverUrl + "session", body);
			}
		}.run(ssn);
		
	}

	//---------------------------------------------------------------------------------------------//
	//                                       /SESSION/ATTR                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Set a session attribute.
	 * @return previous global value of this attribute.
	 * 
	 */
	public String sessionAttrSet(SessionImpl ssn, String name, String value) {
		
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
			jsonGen.flush();
		}
		catch (Exception t) {
			throw new ClientException.Internal(t);
		}

		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.put(serverUrl + "session/attr", body.toString());
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(response.session);
		return response.returns;
	}

	/**
	 * Set a session attribute.
	 * @return previous global value of this attribute.
	 * 
	 */
	public String sessionAttrClear(SessionImpl ssn, String name) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionAttrClear(%s)", name));

		// Make body
		final StringWriter body = new StringWriter(2048);
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("sid", ssn.getId());
			jsonGen.writeStringField("name", name);
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception t) {
			throw new ClientException.Internal(t);
		}

		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.delete(serverUrl + "session/attr", body.toString());
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(response.session);
		return response.returns;
	}

	//---------------------------------------------------------------------------------------------//
	//                                         /REQUEST                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * POST /request.
     * Create a state request by targeting session for a state
	 */
	public void requestCreate(SessionImpl ssn, String state) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCreate(%s,%s)", ssn.getId(), state));

		final String body = String.format("{\"sid\":\"%s\",\"state\":\"%s\"}", ssn.getId(), state);
		
		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.post(serverUrl + "request", body); 
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(response.session);
	}

	/**
	 * PUT /request.
	 * Commit a state request and trigger the state visited event.
	 */
	public boolean requestCommit(final SessionImpl ssn) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCommit(%s)", ssn.getId()));

		Payload.Session response =  new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				StringWriter body = new StringWriter();
				JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
				jsonGen.writeStartObject();
				jsonGen.writeStringField("sid", ssn.getId());
				jsonGen.writeEndObject();
				jsonGen.flush();
				HttpResponse resp = adapter.put(serverUrl + "request", body.toString());
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(response.session);

		return true;
	}

	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void eventSave(final Session ssn, final VariantEvent event) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("eventSave(%s,%s)", ssn.getId(), event.getName()));

		new CommonExceptionHandlerVoid() {
			@Override void voidBlock() throws Exception {
				adapter.post(serverUrl + "event", ((VariantEventSupport)event).toJson());
			}
		}.run(ssn);
	}

}
