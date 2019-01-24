package com.variant.client.impl;

import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.client.SessionExpiredException;
import com.variant.client.TargetingTracker;
import com.variant.client.UnknownSchemaException;
import com.variant.client.VariantException;
import com.variant.client.net.Payload;
import com.variant.client.net.http.HttpAdapter;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.StateRequestStatus;
import com.variant.core.TraceEvent;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.impl.TraceEventSupport;
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
		 * Here we only intercept the session expired exception thrown earlier and
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
			// Intercept certain user exceptions.
			catch (VariantException ce) {
				if (ce.getError() == ServerError.SESSION_EXPIRED) {
					throw new SessionExpiredException(sid);
				}
				else throw ce;
			}

			// Pass through internal exceptions
			catch (VariantException.Internal ce) { 
				throw ce;
			}

			// Something unexpected - wrap as an Internal.
			catch (Exception e) {
				throw new VariantException.Internal("Unexpected Exception", e);
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
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                        /CONNECTION                                          //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Connect this server to a schema denoted by the given URI.
	 * @return
	 */
	Payload.Connection connect(URI uri) {
		
		if (LOG.isTraceEnabled()) LOG.trace("connect()");

		serverUrl = uri.getHost();
		String schema = uri.getPath();
		
		
		try {
			HttpResponse resp = adapter.get(uri.getHost() + "connection/" + uri.getPath());
			return Payload.Connection.parse(resp);
		}
		catch (VariantException ce) {
			if (ce.getError() == ServerError.UNKNOWN_SCHEMA) {
				throw new UnknownSchemaException(schema);
			}
			else throw ce;
		}
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
			throw new VariantException.Internal("Unable to serialize payload", e);
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
	//                                       /SESSION/ATTR                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Set a session attribute.
	 * @return previous global value of this attribute.
	 * 
	 */
	public void sessionAttrSet(SessionImpl ssn, String name, String value) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionAttrSet(%s, %s)", name, value));

		// Body
		StringWriter body = new StringWriter(2048);
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("sid", ssn.getId());
			jsonGen.writeStringField("name", name);
			jsonGen.writeStringField("value", value);
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw new VariantException.Internal("Unable to serialize payload", e);
		}

		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.put(serverUrl + "session/attr", body.toString());
				return Payload.Session.parse(ssn.getConnection(), resp);
			}
		}.run(ssn);
		
		ssn.rewrap(CoreSession.fromJson(response.coreSsnSrc, ssn.getSchema()));

	}

	/**
	 * Set a session attribute.
	 * @return previous global value of this attribute.
	 * 
	 */
	public void sessionAttrClear(SessionImpl ssn, String name) {
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("sessionAttrClear(%s)", name));

		// Body
		StringWriter body = new StringWriter(2048);
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("sid", ssn.getId());
			jsonGen.writeStringField("name", name);
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw new VariantException.Internal("Unable to serialize payload", e);
		}

		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.delete(serverUrl + "session/attr", body.toString());
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
			jsonGen.writeStringField("sid", ssn.getId());
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
			throw new VariantException.Internal("Unable to serialize payload", e);
		}

		Payload.Session response = new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.post(serverUrl + "request", body.toString()); 
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
	public boolean requestCommit(final StateRequestImpl req, StateRequestStatus status) {
		
		SessionImpl ssn = (SessionImpl) req.getSession();
		StateVisitedEvent sve = (StateVisitedEvent) req.getStateVisitedEvent();
		
		if (LOG.isTraceEnabled()) LOG.trace(
				String.format("requestCommit(%s)", ssn.getId()));

		// Body
		StringWriter body = new StringWriter();
		try {
			JsonGenerator jsonGen = new JsonFactory().createGenerator(body);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("sid", ssn.getId());
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
			throw new VariantException.Internal("Unable to serialize payload", e);
		}
		
		Payload.Session response =  new CommonExceptionHandler<Payload.Session>() {
			
			@Override Payload.Session block() throws Exception {
				HttpResponse resp = adapter.put(serverUrl + "request", body.toString());
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
			jsonGen.writeFieldName("event");
			jsonGen.writeRawValue(TraceEventSupport.toJson(event));
			jsonGen.writeEndObject();
			jsonGen.flush();
		}
		catch (Exception e) {
			throw new VariantException.Internal("Unable to serialize payload", e);
		}

		new CommonExceptionHandlerVoid() {
			@Override void voidBlock() throws Exception {
				adapter.post(serverUrl + "event", body.toString());
			}
		}.run(ssn);
	}

	public static void main(String[] args) {
		URI uri = URI.create("//localhost:5377/foo");
		System.out.println(uri.getScheme());
		System.out.println(uri.getHost());
		System.out.println(uri.getPath());
	}
}
