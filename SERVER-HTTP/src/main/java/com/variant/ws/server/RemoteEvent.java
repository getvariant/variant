package com.variant.ws.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.variant.core.event.VariantEvent;

/**
 * Event received over HTTP and suitable to pass to the Core API.
 */
public class RemoteEvent implements VariantEvent {

	private String name;
	private String value;
	private Date createDate = new Date();
	private Map<String,Object> params = new HashMap<String,Object>();
	
	public RemoteEvent(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Date getCreateDate() {
		return createDate;
	}

	@Override
	public String getEventName() {
		return name;
	}

	@Override
	public String getEventValue() {
		return value;
	}

	@Override
	public Map<String, Object> getParameterMap() {
		return params;
	}

	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	public void setCreateDate(Date date) {
		createDate = date;
	}
	
	public void setParameter(String key, String value) {
		params.put(key, value);
	}
}