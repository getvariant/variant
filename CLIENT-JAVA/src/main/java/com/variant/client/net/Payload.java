package com.variant.client.net;

import static com.variant.client.impl.ClientInternalError.NET_PAYLOAD_ELEMENT_MISSING;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.ClientException;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.impl.VariantException;
import com.variant.core.session.CoreSession;


/**
 * Payload and its subtypes.
 * Each paylad type must have a static parse method, called by the payload reader.
 */
abstract public class Payload {
		
	/**
	 */
	public static class Connection extends Payload {
		
		public final int sessionTimeout;
		
		private Connection(int sessionTimeout) {
			this.sessionTimeout = sessionTimeout;
		}
		
		public static Connection parse(HttpResponse resp) {
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String,?> map = mapper.readValue(resp.body, Map.class);
				Integer ssnto = (Integer) map.get("ssnto");
				if (ssnto == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "ssnto", Connection.class.getName());
				
				return new Connection(ssnto);
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
		
		public final String coreSsnSrc;
		public final String schemaId;
		public final String schemaSrc;
		public final String returns;
		
		private Session(String coreSsnSrc, String schemaId, String schemaSrc, String returns) {
			this.coreSsnSrc = coreSsnSrc;
			this.schemaId = schemaId;
			this.schemaSrc = schemaSrc;
			this.returns = returns;			
		}
		
		public static Session parse(com.variant.client.Connection conn, HttpResponse resp) {

			try {

				String coreSsnSrc = null;
				String schemaId = null;
				String schemaSrc = null;
				String returns = null;
				
				ObjectMapper mapper = new ObjectMapper();
				
				@SuppressWarnings("unchecked")
				Map<String,?> map = mapper.readValue(resp.body, Map.class);

				coreSsnSrc = (String) map.get("session");
				if (coreSsnSrc == null)
					throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "session", Session.class.getName());

				returns = (String) map.get("returns");

				@SuppressWarnings("unchecked")
				Map<String,String> schema = (Map<String,String>) map.get("schema");
				if (schema!= null) {
					schemaSrc = schema.get("src");
					schemaId = schema.get("id");
					if (schemaSrc == null)
						throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "schema/src", Connection.class.getName());
					if (schemaId == null)
						throw new ClientException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "schema/id", Connection.class.getName());
				}
				

				return new Session(coreSsnSrc, schemaId, schemaSrc, returns);
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
