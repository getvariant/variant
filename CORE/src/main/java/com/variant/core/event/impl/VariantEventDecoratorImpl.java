package com.variant.core.event.impl;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import com.variant.core.VariantStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.VariantEventDecorator;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.VariantSessionImpl;

/**
 * EVENTS DAO.
 * 
 * @author Igor.
 *
 */
public class VariantEventDecoratorImpl implements VariantEventDecorator {
			
	private VariantStateRequest request;
	private VariantEvent userEvent;
	
	/**
	 * Constructor
	 * @return
	 */
	public VariantEventDecoratorImpl(VariantEvent event, VariantStateRequest request) {
		this.request = request;
		this.userEvent = event;
	}

	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public String getEventName() {
		return userEvent.getEventName();
	}

	@Override
	public String getEventValue() {
		return userEvent.getEventValue();
	}

	@Override
	public Date getCreateDate() {
		return userEvent.getCreateDate();
	}

	@Override
	public Map<String,Object> getParameterMap() {
		return userEvent.getParameterMap();
	}
	
	@Override
	public VariantStateRequest getStateRequest() {
		return request;
	}

	@Override
	public Collection<Experience> getActiveExperiences() {

		VariantSessionImpl session = (VariantSessionImpl) request.getSession();
		
		Collection<Experience> result = new LinkedList<Experience>();
		for (Experience e: request.getTargetedExperiences()) 
			if (session.isQualified(e.getTest())) result.add(e);
		
		return result;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//	
	/**
	 * The event we're wrapping.
	 * @return
	 */
	public VariantEvent getOriginalEvent() {
		return userEvent;
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
		.append("createdOn:'").append(getCreateDate()).append("', ")
		.append("eventName:").append(getEventName()).append("', ")
		.append("eventValue:").append(getEventValue()).append("', ")
		.append("params:{");
		boolean first = true;
		for (Map.Entry<String, Object> e: getParameterMap().entrySet()) {
			if (first) first = false;
			else result.append(",");
			result.append("'").append(e.getKey()).append("':");
			result.append("'").append(e.getValue()).append("'");
		}
		result.append("}");
		return result.toString();

	}

}
