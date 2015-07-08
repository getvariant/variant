package com.variant.core.event;

import com.variant.core.schema.Test;
import com.variant.core.schema.View;

/**
 * EVENTS_EXPERIENCES DAO.
 * 
 * @author Igor.
 */
public class EventExperience {
	
	private long eventId;
	private Test.Experience experience;
	private View view;
	private String path;
	
	/**
	 * 
	 * @param eventId
	 * @param experience
	 * @param view
	 * @param path
	 */
	public EventExperience(long eventId, Test.Experience experience, View view, String path) {
		this.eventId = eventId;
		this.experience = experience;
		this.view = view;
		this.path = path;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getEventId() {
		return eventId;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTestName() {
		return experience.getTest().getName();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getExperienceName() {
		return experience.getName();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isExpereinceControl() {
		return experience.isControl();
	}
	
	/**
	 * 
	 * @return
	 */
	public Boolean isViewInvariant() {
		return view.isInvariantIn(experience.getTest());
	}
	
	/**
	 * 
	 * @return
	 */
	public String getViewResolvedPath() {
		return path;
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public String toString() {
		
		return new StringBuilder()
		.append('{')
		.append("eventId:'").append(getEventId()).append("', ")
		.append("testName:'").append(getTestName()).append("',")
		.append("experienceName:'").append(getExperienceName()).append("', ")
		.append("isExperienceControl:").append(isExpereinceControl()).append(", ")
		.append("isPageVariant:").append(isViewInvariant()).append(", ")
		.append("pageResolvedPath:'").append(getViewResolvedPath()).append("', ")
		.append("}").toString();

	}
}
