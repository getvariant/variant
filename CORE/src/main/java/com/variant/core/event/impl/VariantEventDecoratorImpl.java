package com.variant.core.event.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import com.variant.core.VariantSession;
import com.variant.core.event.VariantEvent;
import com.variant.core.event.PersistableVariantEvent;
import com.variant.core.schema.Test.Experience;

/**
 * EVENTS DAO.
 * 
 * @author Igor.
 *
 */
public class VariantEventDecoratorImpl implements PersistableVariantEvent, Serializable {
			
	/**
	 */
	private static final long serialVersionUID = 1L;

	private VariantSession session;
	private VariantEvent userEvent;
	
	/**
	 * Constructor
	 * @return
	 */
	public VariantEventDecoratorImpl(VariantEvent event, VariantSession session) {
		this.userEvent = event;		
		this.session = session;
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
	public VariantSession getSession() {
		return session;
	}

	@Override
	public Collection<Experience> getActiveExperiences() {
		
		Collection<Experience> result = new LinkedList<Experience>();
		for (Experience e: session.getStateRequest().getTargetedExperiences()) {
		//	if (session.isQualified(e.getTest())) result.add(e);
			result.add(e);
		}
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
		.append("sessionid:'").append(session.getId()).append("', ")
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
