package com.variant.server.event;

import java.util.Date;
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
	private String value;
	private Date createDate = new Date();
	private Map<String,String> attributes = new HashMap<String,String>();
	
	/**
	 * Construct a trace event from scratch
	 */
	public ServerTraceEvent(String name, String value, Date createDate) {
		this.name = name;
		this.value = value;
		this.createDate = createDate;
	}
	
	/**
	 * Construct a trace event from an SVE.
	 */
	public ServerTraceEvent(StateVisitedEvent sve) {
		this.name = sve.getName();
		this.value = sve.getValue();
		this.createDate = sve.getCreateDate();
		attributes.putAll(sve.getAttributes());
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Date getCreateDate() {
		return createDate;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
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
	public String setAttribute(String key, String val) {
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