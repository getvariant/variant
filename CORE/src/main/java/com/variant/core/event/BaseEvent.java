package com.variant.core.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.variant.core.VariantSession;
import com.variant.core.config.Test;

/**
 * EVENTS DAO.
 * 
 * @author Igor.
 *
 */
abstract public class BaseEvent {
		
	protected List<Test.Experience> experiences = new ArrayList<Test.Experience>();
	
	protected long id;
	protected String sessionId;
	protected Date createDate;
	protected Status status;
	protected String eventName;
	protected String eventValue;
	
	/**
	 * Constructor
	 * @return
	 */
	protected BaseEvent(VariantSession session, String eventName, String eventValue, Status status) {
		this.sessionId = session.getId();
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
	
	/**
	 * Subclasses must provide a way to get to event-experiences.
	 * @return
	 */
	abstract public Collection<EventExperience> getEventExperiences();
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public Status getStatus() {
		return status;
	}

	public String getEventName() {
		return eventName;
	}

	public String getEventValue() {
		return eventValue;
	}
	
	public static enum Status {
		SUCCESS, 
		EXCEPTION
	}

}
