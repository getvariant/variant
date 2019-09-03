package com.variant.server.impl;

import java.util.HashMap;
import java.util.Map;

import com.variant.core.util.CollectionsUtils;
import com.variant.server.api.TraceEvent;

/**
 * Event received over HTTP and suitable to pass to the Core API.
 */
public class TraceEventImpl implements TraceEvent {

	private String name;
	private Map<String,String> attributes = new HashMap<String,String>();
	
	/**
	 * Construct a trace event from scratch
	 */
	public TraceEventImpl(String name) {
		this.name = name;
	}

	/**
	 * Construct a trace event from scratch
	 */
	public TraceEventImpl(String name, Map<String,String> attributes) {
		this(name);
		this.attributes.putAll(attributes);		
	}
	
	//---------------------------------------------------------------------------------------------//
	//                             Static Convenience Factory Method                              //
	//---------------------------------------------------------------------------------------------//
	public static TraceEvent mkTraceEvent(String name) {
		return new TraceEvent() {

			private HashMap<String, String> attributes = new HashMap<String,String>();
			
			@Override
			public String getName() {
				return name;
			}

			@Override
			public Map<String, String> getAttributes() {
				return attributes;
			}
		};
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
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Map<String,String> getAttributes() {
		return attributes;
	}

}