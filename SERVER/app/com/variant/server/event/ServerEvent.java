package com.variant.server.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.variant.core.TraceEvent;

/**
 * Event received over HTTP and suitable to pass to the Core API.
 */
public class ServerEvent implements TraceEvent {

	private String name;
	private String value;
	private Date createDate = new Date();
	private Map<String,String> params = new HashMap<String,String>();
	
	public ServerEvent(String name, String value, Date createDate) {
		this.name = name;
		this.value = value;
		this.createDate = createDate;
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
	public Map<String, String> getParameterMap() {
		return params;
	}

	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//
	
	public void setParameter(String key, String value) {
		params.put(key, value);
	}
}