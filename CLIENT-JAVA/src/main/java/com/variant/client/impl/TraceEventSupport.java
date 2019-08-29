package com.variant.client.impl;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.variant.client.TraceEvent;
import com.variant.core.error.CoreException;

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
	public Map<String,String> getAttributes() {
		return attributes;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                  Static Factory Methods                                     //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Factory method returns a blank custom trace event with a given name. This event can be triggered by passing
	 * it to <code>Session.triggerTraceEvent()</code>.
	 * @param name Name of the trace event to be created
	 * @return an implementation of this interface.
	 * 
	 *  @since 0.9
	 */
	static public TraceEvent mkTraceEvent(String name) {
		return new TraceEventSupport(name) {};
	}

	/**
	 * Factory method returns a custom trace event with a given name and event attributes. This event can be triggered by passing
	 * it to <code>Session.triggerTraceEvent()</code>.
	 * @param name Name of the trace event to be created
	 * @return an implementation of this interface.
	 * 
	 *  @since 0.9
	 */
	static public TraceEvent mkTraceEvent(String name, Map<String,String> attributes) {
		TraceEvent result = mkTraceEvent(name);
		result.getAttributes().putAll(attributes);
		return result;
	}

	//---------------------------------------------------------------------------------------------//
	//                                       Serialization                                          //
	//---------------------------------------------------------------------------------------------//
	
	private static final String FIELD_NAME_NAME = "name";
	private static final String FIELD_NAME_ATTRIBUTES = "attrs";

	/**
	 * Serialize to JSON string.
	 * @return
	 * @throws JsonProcessingException 
	 *
	public static String toJson(TraceEvent event) {
		try {
			StringWriter result = new StringWriter(1024);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField(FIELD_NAME_NAME, event.getName());
			
			if (!event.getAttributes().isEmpty()) {
				jsonGen.writeObjectFieldStart(FIELD_NAME_ATTRIBUTES);
				for (Map.Entry<String,String> e: event.getAttributes().entrySet()) {
					jsonGen.writeStringField(e.getKey(), e.getValue());
				}
				jsonGen.writeEndObject();
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
	 * 
	 *
	@Override
	public String toString() {
		return toJson(this);
	}
	**/
}