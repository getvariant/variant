package com.variant.server.event;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.variant.core.TraceEvent;
import com.variant.core.schema.Test.Experience;
import com.variant.core.session.CoreSession;
import com.variant.server.api.FlushableTraceEvent;
import com.variant.server.api.Session;

/**
 * Flushable event implementation suitable for the server.
 * 
 * @author Igor.
 *
 */
public class FlushableTraceEventImpl implements FlushableTraceEvent, Serializable {
			
	/**
	 */
	private static final long serialVersionUID = 1L;

	private Session session;
	private TraceEvent userEvent;
	
	/**
	 * Constructor
	 * @return
	 */
	public FlushableTraceEventImpl(TraceEvent event, Session session) {
		this.userEvent = event;		
		this.session = session;
	}

	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
			
	@Override
	public String getName() {
		return userEvent.getName();
	}

	@Override
	public String getValue() {
		return userEvent.getValue();
	}

	@Override
	public Date getCreateDate() {
		return userEvent.getCreateDate();
	}

	@Override
	public Map<String,String> getParameterMap() {
		return userEvent.getParameterMap();
	}
	
	@Override
	public Session getSession() {
		return session;
	}

	@Override
	public Set<Experience> getLiveExperiences() {
		
		Set<Experience> result = new HashSet<Experience>();
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
	public TraceEvent getOriginalEvent() {
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
		.append("eventName:").append(getName()).append("', ")
		.append("eventValue:").append(getValue()).append("', ")
		.append("params:{");
		boolean first = true;
		for (Map.Entry<String, String> e: getParameterMap().entrySet()) {
			if (first) first = false;
			else result.append(",");
			result.append("'").append(e.getKey()).append("':");
			result.append("'").append(e.getValue()).append("'");
		}
		result.append("}");
		return result.toString();

	}

}
