package com.variant.core.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.variant.core.TraceEvent;

/**
 * 
 * @author Igor.
 */
abstract public class TraceEventSupport implements TraceEvent {
		
	protected Map<String, String> attributes = new HashMap<String, String>();
	protected String name;
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	/**
	 */
	public TraceEventSupport(String name) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String setAttribute(String key, String value) {
		return attributes.put(key, value);
	}

	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}
	
	@Override
	public String clearAttribute(String key) {
		return attributes.remove(key);
	}

	//---------------------------------------------------------------------------------------------//
	//                                       Serialization                                          //
	//---------------------------------------------------------------------------------------------//
	
	private static final String FIELD_NAME_NAME = "name";
	private static final String FIELD_NAME_ATTRIBUTES = "attrs";

	/**
	 * Static Method for convenience.
	 * @param event
	 * @return
	 */
	public static String toJson(TraceEvent event) {
		return ((TraceEventSupport)event).toJson();
	}

	/**
	 * Serialize to JSON string.
	 * @return
	 * @throws JsonProcessingException 
	 */
	public String toJson() {
		try {
			StringWriter result = new StringWriter(1024);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField(FIELD_NAME_NAME, getName());
			
			if (!attributes.isEmpty()) {
				jsonGen.writeArrayFieldStart(FIELD_NAME_ATTRIBUTES);
				for (Map.Entry<String,String> e: attributes.entrySet()) {
					jsonGen.writeStartObject();
					jsonGen.writeStringField(e.getKey(), e.getValue());
					jsonGen.writeEndObject();
				}
				jsonGen.writeEndArray();
			}
			
			jsonGen.writeEndObject();
			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new CoreException.Internal("Unable to serialize session", e);
		}
	}
	
	/**
	 * Deserialize from JSON string.
	 * We can't instantiate here, so the caller will instantiate and pass us a shell of an
	 * concrete implemantion object and we'll breath life into it. The SID is already set.
	 * @param json
	 * @return
	 * Don't think we're deserializing events on the client any more. (Local object)
	public static <T extends TraceEventSupport> T fromJson(T result, Map<String,?> mappedJson) {
		
		result.name = (String) mappedJson.get(FIELD_NAME_NAME);
		result.value = (String) mappedJson.get(FIELD_NAME_VALUE);
		
		@SuppressWarnings("unchecked")
		List<Map<String,String>> params = (List<Map<String,String>>) mappedJson.get(FIELD_NAME_ATTRIBUTES);
		if (params != null) {
			for (Map<String,String> p: params) {
				result.attributes.put(p.get(FIELD_NAME_KEY), p.get(FIELD_NAME_VALUE));
			}
		}
		return result;
	}
	*/ 
	/**
	 * 
	 */
	@Override
	public String toString() {
		return toJson();
	}
}
