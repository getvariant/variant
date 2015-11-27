package com.variant.core.event;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.Test.Experience;

/**
 * EVENTS DAO.
 * 
 * @author Igor.
 *
 */
abstract public class VariantEventSupport implements VariantEvent {
		
	protected Collection<Experience> experiences;
	protected Map<String, Object> params = new HashMap<String, Object>();
	
	protected Long id = null;
	protected VariantStateRequest request;
	protected Date createDate;
	protected String eventName;
	protected String eventValue;
	
	/**
	 * Constructor
	 * @return
	 */
	protected VariantEventSupport(VariantStateRequest request, String eventName, String eventValue) {
		this.request = request;
		this.createDate = new Date();
		this.eventName = eventName;
		this.eventValue = eventValue;
		this.experiences = request.getTargetedExperiences();
		if (experiences.size() == 0) throw new VariantInternalException("Must pass at least one experience");
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
		return request.getSession();
	}

	@Override
	public Date getCreateDate() {
		return createDate;
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
		.append("sessionid:'").append(request.getSession().getId()).append("', ")
		.append("createdOn:'").append(createDate).append("', ")
		.append("eventName:").append(eventName).append("', ")
		.append("eventValue:").append(eventValue).append("', ")
		.append("}").toString();

	}

}
