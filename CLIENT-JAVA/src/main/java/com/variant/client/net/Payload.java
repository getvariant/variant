package com.variant.client.net;

import static com.variant.client.impl.ClientInternalError.NET_PAYLOAD_ELEMENT_MISSING;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.VariantException;
import com.variant.client.impl.ClientInternalError;
import com.variant.client.net.http.HttpResponse;


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
					throw new VariantException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "ssnto", Connection.class.getName());
				
				return new Connection(ssnto);
			}
			catch (VariantException va) {
				throw va;
			}
			catch (Throwable t) {
				throw new VariantException(t, 
						ClientInternalError.INTERNAL_ERROR, String.format("Unable to parse payload type [%s]", Session.class.getName()));
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
		
		private Session(String coreSsnSrc, String schemaId, String schemaSrc) {
			this.coreSsnSrc = coreSsnSrc;
			this.schemaId = schemaId;
			this.schemaSrc = schemaSrc;
		}
		
		public static Session parse(com.variant.client.Connection conn, HttpResponse resp) {

			try {

				String coreSsnSrc = null;
				String schemaId = null;
				String schemaSrc = null;
				
				ObjectMapper mapper = new ObjectMapper();
				
				@SuppressWarnings("unchecked")
				Map<String,?> map = mapper.readValue(resp.body, Map.class);

				coreSsnSrc = (String) map.get("session");
				if (coreSsnSrc == null)
					throw new VariantException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "session", Session.class.getName());

				@SuppressWarnings("unchecked")
				Map<String,String> schema = (Map<String,String>) map.get("schema");
				if (schema!= null) {
					schemaSrc = schema.get("src");
					schemaId = schema.get("id");
					if (schemaSrc == null)
						throw new VariantException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "schema/src", Connection.class.getName());
					if (schemaId == null)
						throw new VariantException.Internal(NET_PAYLOAD_ELEMENT_MISSING, "schema/id", Connection.class.getName());
				}
				

				return new Session(coreSsnSrc, schemaId, schemaSrc);
			}
			catch (VariantException va) {
				throw va;
			}
			catch (Throwable t) {
					throw new VariantException(
							ClientInternalError.INTERNAL_ERROR, String.format("Unable to parse payload type [%s]", Session.class.getName()), t);
			}
		}
	}
}
