package com.variant.core.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.variant.core.VariantEvent;
import com.variant.core.VariantSession;
import com.variant.core.schema.Test;

/**
 * EVENTS DAO.
 * 
 * @author Igor.
 *
 */
abstract public class VariantEventSupport implements VariantEvent {
		
	protected List<Test.Experience> experiences = new ArrayList<Test.Experience>();
	protected Map<String, Object> params = new HashMap<String, Object>();
	
	protected Long id = null;
	protected VariantSession session;
	protected Date createDate;
	protected Status status;
	protected String eventName;
	protected String eventValue;
	
	/**
	 * Constructor
	 * @return
	 */
	protected VariantEventSupport(VariantSession session, String eventName, String eventValue, Status status) {
		this.session = session;
		this.createDate = new Date();
		this.eventName = eventName;
		this.eventValue = eventValue;
		this.status = status;
	}

	/**
	 * 
	 * @param experience
	 */
	protected void addExperience(Test.Experience experience) {
		experiences.add(experience);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
		
	@Override
	public Long getId() {
		return id;
	}
	
	@Override
	public VariantSession getSession() {
		return session;
	}

	@Override
	public Date getCreateDate() {
		return createDate;
	}

	@Override
	public Status getStatus() {
		return status;
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
	public Object setParameter(String key, Object value) {
		return params.put(key, value);
	}
	
	@Override
	public Object getParameter(String key) {
		return params.get(key);
	}
	
	@Override
	public Set<String> getParameterKeys() {
		return params.keySet();
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		
		return new StringBuilder()
		.append('{')
		.append("sessionid:'").append(session.getId()).append("', ")
		.append("createdOn:'").append(createDate).append("', ")
		.append("eventName:").append(eventName).append("', ")
		.append("eventValue:").append(eventValue).append("', ")
		.append("status:").append(status)
		.append("}").toString();

	}

}
