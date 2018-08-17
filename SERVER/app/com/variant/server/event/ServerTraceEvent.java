package com.variant.server.event;

import java.util.HashMap;
import java.util.Map;

import com.variant.core.TraceEvent;
import com.variant.core.impl.StateVisitedEvent;
import com.variant.core.util.immutable.ImmutableMap;

/**
 * Event received over HTTP and suitable to pass to the Core API.
 */
public class ServerTraceEvent  implements TraceEvent {

	private String name;
	private Map<String,String> attributes = new HashMap<String,String>();
	
	/**
	 * Construct a trace event from scratch
	 */
	public ServerTraceEvent(String name) {
		this.name = name;
	}

	/**
	 * Construct a trace event from scratch
	 */
	public ServerTraceEvent(String name, Map<String,String> attributes) {
		this(name);
		this.attributes.putAll(attributes);		
	}
	
	/**
	 * Construct a trace event from an SVE.
	 */
	public ServerTraceEvent(StateVisitedEvent sve) {
		this(sve.getName(), sve.getAttributes());
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String clearAttribute(String key) {
		return attributes.remove(key);
	}

	@Override
	public String getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public String setAttribute(String key, String value) {
		return attributes.put(key, value);
	}

	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//
	
	public void setParameter(String key, String value) {
		attributes.put(key, value);
	}

	public Map<String, String> getAttributes() {
		return new ImmutableMap<String, String>(attributes);
	}

}