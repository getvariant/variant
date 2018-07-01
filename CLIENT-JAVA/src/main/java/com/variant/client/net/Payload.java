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
		
		public final CoreSession session;
		public final String returns;
		
		private Session(CoreSession session, String returns) {
			this.session = session;
			this.returns = returns;
		}
		
		public static Session parse(com.variant.client.Connection conn, HttpResponse resp) {

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
