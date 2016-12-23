package com.variant.server.event;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.variant.core.VariantEvent;

/**
 * Event received over HTTP and suitable to pass to the Core API.
 */
public class ServerEvent implements VariantEvent {

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
	public String getEventName() {
		return name;
	}

	@Override
	public String getEventValue() {
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