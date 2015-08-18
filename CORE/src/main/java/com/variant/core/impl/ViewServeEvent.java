package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.variant.core.VariantEventExperience;
import com.variant.core.VariantSession;
import com.variant.core.event.VariantEventSupport;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;


public class ViewServeEvent extends VariantEventSupport {

	private static final String EVENT_NAME = "VIEW_SERVE";
	private static final String PARAM_NAME_VIEW_RESOLVED_PATH = "VIEW_RESOLVED_PATH";
	
	private View view;
	
	/**
	 * New constructor
	 */
	ViewServeEvent(View view, VariantSession session, String viewResolvedPath, Collection<Experience> experiences) {
		super(session, EVENT_NAME, view.getName(), experiences);
		this.view = view;
		setParameter(PARAM_NAME_VIEW_RESOLVED_PATH, viewResolvedPath);

	}
	
	/**
	 * 
	 * @return
	 */
	public View getView() {
		return view;
	}
	
	/**
     *
	 */
	@Override
	public Collection<VariantEventExperience> getEventExperiences() {
		
		Collection<VariantEventExperience> result = new ArrayList<VariantEventExperience>();
		
		for (Test.Experience exp: experiences) {
			result.add(new ViewServeEventExperience(this, exp));
		}
		
		return Collections.unmodifiableCollection(result);
	}

		
}
