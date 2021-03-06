package com.variant.client.impl;

import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.client.SessionExpiredException;
import com.variant.client.StateRequest;
import com.variant.client.TargetingTracker;
import com.variant.client.TraceEvent;
import com.variant.client.UnknownSchemaException;
import com.variant.client.VariantClient;
import com.variant.client.VariantException;
import com.variant.client.net.Payload;
import com.variant.client.net.http.HttpAdapter;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.error.ServerError;
import com.variant.core.session.CoreSession;

/**
 * The abstraction of the remote server.  
 * One server per connection, one connection per server.
 * 
 * @author Igor
 */
public class Server {

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	private final HttpAdapter adapter;
	private String serverUrl;
	
	/**
	 * All outbound operations which expect a return type.
	 *
	 * @param <T>
	 */
	private abstract class CommonExceptionHandler<T> {
		
		abstract T block() throws Exception;
		
		/**
		 * Call to server backed by a local session object.
		 * All sessions should use this. All real work happens in {@link #run(String)}.
		 * Here we only intercept the session expired exception thrown earlier
		 * and mark session expired.
		 */
		T run(SessionImpl ssn) {
			try {
				return run(ssn.getId());
			}
			catch (SessionExpiredException x) {
				ssn.expire();
				throw x;
			}
		}

	   /**
		 *  Call to server with no local session, only sid.
		 */
		T run(String sid) {
			
			try { 
				return block(); 
			}
			catch (VariantException ve) {
			   // Transform most likely exceptions into their own type.
				if (ve.error == ServerError.SESSION_EXPIRED) throw new SessionExpiredException(sid);
				else if (ve.error == ServerError.UNKNOWN_SCHEMA) throw  new UnknownSchemaException(ve.args[0]);         
				else throw ve;
			}

			// Should never happen at this point.
			catch (Exception e) {
				throw VariantException.internal("Unexpected Exception", e);
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
	
	/**
	 */
	public Server(VariantClient client) {
		this.adapter = new HttpAdapter();
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          /SCHEMA                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Connect this server to a schema denoted by the given URI.
	 * @return
	 */
	public Payload.Connection connect(URI variantUri) {
		
		if (LOG.isTraceEnabled()) LOG.trace("connect()");

		serverUrl = "http://" + variantUri.getHost() + ":" + variantUri.getPort() + "/";
		String schema = variantUri.getPath().substring(1); // remove the leading /
		
		try {
			HttpResponse resp = adapter.get(serverUrl + "schema/" + schema);
			return Payload.Connection.parse(resp);
		}
		catch (VariantException ce) {
			if (ce.error == ServerError.UNKNOWN_SCHEMA) {
				throw new UnknownSchemaException(schema);
			}
			else throw ce;
		}
	}
			
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
				HttpResponse resp = adapter.get(serverUrl + "session/" + conn.getSchemaName() + "/" + sid);
				return Payload.Session.parse(conn, resp);
			}
		}.run(sid);
	}

	/**
	 * POST /session/:schema/:sid
	 * Get or create an existing session by ID.
	 * Same URL as GET. Body contains the initial targeting.
	 */
	public Payload.Session sessionGetOrCreate(String sid, ConnectionImpl conn, TargetingTracker tt) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionGetOrCreate(%s)", sid));

		// Body
		StringWriter body = new StringWriter(2048);
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeArrayFieldStart("tt");
			if (tt.get() != null) {
				for (TargetingTracker.Entry e: tt.get()) {
					jsonGen.writeString(e.getTestName() + '.' + e.getExperienceName() + '.' + e.getTimestamp());
				}
			}
			jsonGen.writeEndArray();
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw VariantException.internal("Unable to serialize payload", e);
		}
		
		return new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.post(serverUrl + "session/" + conn.getSchemaName() + "/" + sid, body.toString());
				return Payload.Session.parse(conn, resp);
			}
		}.run(sid);
	}

	/**
	 * Save or replace session on server.
	 * Is this actually needed?
	 */
	public void sessionSave(final SessionImpl ssn) {
		
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
	//                                       /SESSION-ATTR                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Send given session's attribute map to the shared state.
	 */
	public void sessionAttrMapSend(SessionImpl ssn) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionAttrMapSend()"));

		// Body
		StringWriter body = new StringWriter(2048);
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeObjectFieldStart("attrs");
			for (Map.Entry<String, String> e: ssn.getCoreSession().getAttributes().entrySet()) {
				jsonGen.writeStringField(e.getKey(), e.getValue());
			}
			jsonGen.writeEndObject();
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw VariantException.internal("Unable to serialize payload", e);
		}

		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
            String uri = serverUrl + "session-attr/" + ssn.getConnection().getSchemaName() + "/" + ssn.getId();
				HttpResponse resp = adapter.put(uri, body.toString());
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(CoreSession.fromJson(response.coreSsnSrc, ssn.getSchema()));
	}

	/**
	 * Remove given attributes from the shares state.
	 */
	public void sessionAttrRemoveAll(SessionImpl ssn, String...names) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionAttrMapSync()"));

		// Body
		StringWriter body = new StringWriter(2048);
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeArrayFieldStart("attrs");
			for (String name: names) {
				jsonGen.writeString(name);
			}
			jsonGen.writeEndArray();
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw VariantException.internal("Unable to serialize payload", e);
		}
		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
            String uri = serverUrl + "session-attr/" + ssn.getConnection().getSchemaName() + "/" + ssn.getId();
				HttpResponse resp = adapter.delete(uri, body.toString());
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(CoreSession.fromJson(response.coreSsnSrc, ssn.getSchema()));
	}
	//---------------------------------------------------------------------------------------------//
	//                                         /REQUEST                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * POST /request.
	 */
	public void requestCreate(SessionImpl ssn, String state) {

		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCreate(%s,%s)", ssn.getId(), state));
		
		// Body
		StringWriter body = new StringWriter();
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("state", state);
			// If this is the first target request, we need to send the content
			// of the targeting tracker, because it's not yet reflected in the
			// shared state on the server. When server created this session,
			// it didn't have this information.
			if (ssn.getStateRequest() == null) {
				jsonGen.writeArrayFieldStart("stab");
				if (ssn.targetingTracker.get() != null) {
					for (TargetingTracker.Entry e: ssn.targetingTracker.get()) {
						jsonGen.writeString(e.toString());
					}
				}
				jsonGen.writeEndArray();
			}
			
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw VariantException.internal("Unable to serialize payload", e);
		}

		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
			   String uri = serverUrl + "request/" + ssn.getConnection().getSchemaName() + "/" + ssn.getId();
				HttpResponse resp = adapter.post(uri, body.toString()); 
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);

		// If no errors from the server, remove existing state request and rewrap.
		ssn.clearStateRequest();
		ssn.rewrap(CoreSession.fromJson(response.coreSsnSrc, ssn.getSchema()));
		
	}

	/**
	 * PUT /request.
	 * Commit (or fail) a state request and trigger the SVE.
	 * Note, theat the SVE is a local object and we don't marshal it entirely,
	 * only the attributes and only at commit time.
	 * 
	 */
	public boolean requestCommit(final StateRequestImpl req, StateRequest.Status status) {
		
		SessionImpl ssn = (SessionImpl) req.getSession();
		StateVisitedEvent sve = (StateVisitedEvent) req.getStateVisitedEvent();
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCommit(%s)", ssn.getId()));

		// Body
		StringWriter body = new StringWriter();
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeNumberField("status", status.ordinal());
			if (sve != null && sve.getAttributes().size() > 0) {
				jsonGen.writeObjectFieldStart("attrs");
				for (Map.Entry<String, String> e: sve.getAttributes().entrySet()) {
					jsonGen.writeStringField(e.getKey(), e.getValue());
				}
				jsonGen.writeEndObject();
			}
	
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw VariantException.internal("Unable to serialize payload", e);
		}
		
		Payload.Session response =  new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
			   String uri = serverUrl + "request/" + ssn.getConnection().getSchemaName() + "/" + ssn.getId();
				HttpResponse resp = adapter.delete(uri, body.toString());
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(CoreSession.fromJson(response.coreSsnSrc, ssn.getSchema()));

		return true;
	}

	//---------------------------------------------------------------------------------------------//
	//                                           /EVENT                                            //
	//---------------------------------------------------------------------------------------------//

	public void eventSave(final SessionImpl ssn, final TraceEvent event) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("eventSave(%s, %s)", ssn.getId(), event.getName()));

		// Body
		StringWriter body = new StringWriter();
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("sid", ssn.getId());
         jsonGen.writeStringField("name", event.getName());
         if (!event.getAttributes().isEmpty()) {
            jsonGen.writeObjectFieldStart("attrs");
            for (Map.Entry<String,String> e: event.getAttributes().entrySet()) {
               jsonGen.writeStringField(e.getKey(), e.getValue());
            }
            jsonGen.writeEndObject();
         }
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw VariantException.internal("Unable to serialize payload", e);
		}

		new CommonExceptionHandlerVoid() {
			@Override void voidBlock() throws Exception {
            String uri = serverUrl + "event/" + ssn.getConnection().getSchemaName() + "/" + ssn.getId();
				adapter.post(uri, body.toString());
			}
		}.run(ssn);
	}
	
}
