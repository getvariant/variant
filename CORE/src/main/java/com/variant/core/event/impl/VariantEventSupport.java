package com.variant.core.event.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;

/**
 * EVENTS DAO.
 * 
 * @author Igor.
 *
 */
abstract public class VariantEventSupport implements VariantEvent {
		
	protected Map<String, Object> params = new HashMap<String, Object>();
	
	protected VariantStateRequest request;
	protected Date createDate;
	protected String eventName;
	protected String eventValue;
	
	/**
	 * Constructor
	 * @return
	 */
	protected VariantEventSupport(String eventName, String eventValue, VariantStateRequest request) {
		this.request = request;
		this.createDate = new Date();
		this.eventName = eventName;
		this.eventValue = eventValue;
	}

	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public VariantSession getSession() {
		return request.getSession();
	}

	@Override
	public String getEventName() {
		return eventName;
	}

	@Override
	public String getEventValue() {
		return eventValue;
	}

	@Override
	public Date getCreateDate() {
		return createDate;
	}

	@Override
	public Map<String,Object> getParameterMap() {
		return params;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void setParameter(String key, Object value) {
		params.put(key, value);
	}
	
	/**
	 * 
	 * @return
	 */
	public VariantStateRequest getStateRequest() {
		return request;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		
		StringBuilder result = new StringBuilder()
		.append('{')
		.append("sessionid:'").append(request.getSession().getId()).append("', ")
		.append("createdOn:'").append(createDate).append("', ")
		.append("eventName:").append(eventName).append("', ")
		.append("eventValue:").append(eventValue).append("', ")
		.append("params:{");
		boolean first = true;
		for (Map.Entry<String, Object> e: params.entrySet()) {
			if (first) first = false;
			else result.append(",");
			result.append("'").append(e.getKey()).append("':");
			result.append("'").append(e.getValue()).append("'");
		}
		result.append("}");
		return result.toString();

	}

}
