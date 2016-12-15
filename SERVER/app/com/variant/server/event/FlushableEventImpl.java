package com.variant.server.event;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import com.variant.core.api.VariantEvent;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreSession;

/**
 * Flushable event implementation suitable for the server.
 * 
 * @author Igor.
 *
 */
public class FlushableEventImpl implements FlushableEvent, Serializable {
			
	/**
	 */
	private static final long serialVersionUID = 1L;

	private CoreSession session;
	private VariantEvent userEvent;
	
	/**
	 * Constructor
	 * @return
	 */
	public FlushableEventImpl(VariantEvent event, CoreSession session) {
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
	public CoreSession getSession() {
		return session;
	}

	@Override
	public Collection<Experience> getLiveExperiences() {
		
		Collection<Experience> result = new LinkedList<Experience>();
		for (Experience e: session.getStateRequest().getLiveExperiences()) {
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
