package com.variant.client.net;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.variant.client.net.http.HttpResponse;
import com.variant.core.VariantException;


/**
 * Payload and its subtypes.
 * Each paylad type must have a static parse method, called by the payload reader.
 */
abstract public class Payload {
	
	/**
	 */
	public static class Connection extends Payload {
		
		public final String id;
		public final long timestamp;
		public final String schemaSrc;
		
		private Connection(String id, long timestamp, String schemaSrc) {
			this.id = id;
			this.timestamp = timestamp;
			this.schemaSrc = schemaSrc;
		}
		
		public static Connection fromResponse(HttpResponse resp) {
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				@SuppressWarnings("unchecked")
				Map<String,?> map = mapper.readValue(resp.body, Map.class);
				String id = (String) map.get("id");
				Long ts = (Long) map.get("ts");
				String schemaSrc = (String) map.get("schema");
				if (id == null)
					throw new VariantException(String.format("Unable to parse payload type [%s]: 'id' is null", Connection.class.getName()));
				if (ts == null)
					throw new VariantException(String.format("Unable to parse payload type [%s]: 'ts' is null", Connection.class.getName()));
				if (schemaSrc == null)
					throw new VariantException(String.format("Unable to parse payload type [%s]: 'schema' is null", Connection.class.getName()));
				
				return new Connection(id, ts, schemaSrc);
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
	 * 
	 */
	public static class Session extends Payload {
		
		protected Payload parse(Map<String,?> mappedJson) {
			return null;
		}
	}

}
