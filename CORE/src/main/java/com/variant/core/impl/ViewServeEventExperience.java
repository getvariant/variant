package com.variant.core.impl;

import com.variant.core.event.VariantEventExperienceSupport;
import com.variant.core.schema.Test.Experience;

/**
 * 
 * @author Igor
 *
 */
public class ViewServeEventExperience extends VariantEventExperienceSupport {

	private static final String PARAM_NAME_IS_EXPERIENCE_CONTROL = "IS_EXPERIENCE_CONTROL";
	private static final String PARAM_NAME_IS_VIEW_NONVARIANT = "IS_VIEW_NONVARIANT";

	/**
	 * 
	 * @param event
	 * @param experience
	 */
	protected ViewServeEventExperience(ViewServeEvent event, Experience experience) {
		super(event, experience);
		setParameter(PARAM_NAME_IS_EXPERIENCE_CONTROL, experience.isControl());	
		setParameter(PARAM_NAME_IS_VIEW_NONVARIANT, event.getView().isNonvariantIn(experience.getTest()));	
	}

}
