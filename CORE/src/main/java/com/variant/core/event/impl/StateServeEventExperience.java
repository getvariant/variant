package com.variant.core.event.impl;

import com.variant.core.schema.Test.Experience;

/**
 * 
 * @author Igor
 *
 */
public class StateServeEventExperience extends VariantEventExperienceSupport {

	private static final String PARAM_NAME_IS_STATE_NONVARIANT = "IS_STATE_NONVARIANT";

	/**
	 * 
	 * @param event
	 * @param experience
	 */
	protected StateServeEventExperience(StateServeEvent event, Experience experience) {
		super(event, experience);
		setParameter(PARAM_NAME_IS_STATE_NONVARIANT, event.getViewRequest().getState().isNonvariantIn(experience.getTest()));	
	}

}
