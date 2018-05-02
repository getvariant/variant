package com.variant.client.net;

import static com.variant.client.impl.ClientInternalError.NET_PAYLOAD_ELEMENT_MISSING;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.ClientException;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.VariantException;
import com.variant.core.session.CoreSession;


/**
 * Payload and its subtypes.
 * Each paylad type must have a static parse method, called by the payload reader.
 */
abstract public class Payload {
		
	/**
	 */
	public static class Connection extends Payload {
		
		public final String id;
		public final int sessionTimeout;
		public final long timestamp;
		public final String schemaId;
		public final String schemaSrc;
		
		private Connection(String id, int sessionTimeout, long timestamp, String schemaSrc, String schemaId) {
			this.id = id;
			this.sessionTimeout = sessionTimeout;
			this.timestamp = timestamp;
			this.schemaId = schemaId;
			this.schemaSrc = schemaSrc;
		}
		
		public static Connection fromResponse(HttpResponse resp) {
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String,?> map = mapper.readValue(resp.body, Map.class);
				String id = (String) map.get("id");
				Integer ssnto = (Integer) map.get("ssnto");
				Long ts = (Long) map.get("ts");
				@SuppressWarnings("unchecked")
				Map<String,String> schema = (Map<String,String>) map.get("schema");
				String schemaSrc = schema.get("src");
				String schemaId = schema.get("id");
				if (id == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "id", Connection.class.getName());
				if (ts == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "ts", Connection.class.getName());
				if (ssnto == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "ssnto", Connection.class.getName());
				if (schemaSrc == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "schema/src", Connection.class.getName());
				if (schemaId == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "schema/id", Connection.class.getName());
				
				return new Connection(id, ssnto, ts, schemaSrc, schemaId);
			}
			catch (VariantException va) {
				throw va;
			}
			catch (Throwable t) {
					throw new VariantException(String.format("Unable to parse payload type [%s]", Connection.class.getName()), t);
			}
		}
		
	}
	
	/**
	 * Most responses will contain the most up-to-date session, and some will also contain a return value.
	 */
	public static class Session extends Payload {
		
		public final CoreSession session;
		public final String returns;
		
		private Session(CoreSession session, String returns) {
			this.session = session;
			this.returns = returns;
		}
		
		public static Session fromResponse(com.variant.client.Connection conn, HttpResponse resp) {

			try {
				ObjectMapper mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String,?> map = mapper.readValue(resp.body, Map.class);
				String coreSsnSrc = (String) map.get("session");
				if (coreSsnSrc == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "session", Session.class.getName());
				String returns = (String) map.get("returns");

				return new Session(CoreSession.fromJson(coreSsnSrc, conn.getSchema()), returns);
			}
			catch (VariantException va) {
				throw va;
			}
			catch (Throwable t) {
					throw new VariantException(String.format("Unable to parse payload type [%s]", Session.class.getName()), t);
			}
		}
	}

}
