package com.variant.core.event.impl;

import com.variant.core.event.VariantEvent;
import com.variant.core.event.VariantEventVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

/**
 * 
 * @author Igor
 *
 */
public class StateServeEventVariant implements VariantEventVariant{

	private long id;
	private StateServeEvent event;
	private Test.Experience experience;

	protected StateServeEventVariant(StateServeEvent event, Experience experience) {
		this.event = event;
		this.experience = experience;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public VariantEvent getEvent() {
		return event;
	}
	
	@Override
	public Experience getExperience() {
		return experience;
	}
	
	@Override
	public boolean isExperienceControl() {
		return experience.isControl();
	}

	@Override
	public boolean isStateNonvariant() {
		return event.getStateRequest().getState().isNonvariantIn(experience.getTest());
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	/**
	 * 
	 * @param id
	 */
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
		.append("eventId:'").append(getEvent().getId()).append("', ")
		.append("experience:'").append(getExperience()).append("', ")
		.append("isExperienceControl:").append(getExperience().isControl())
		.append("}").toString();

	}

}