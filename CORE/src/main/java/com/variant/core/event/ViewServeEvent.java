package com.variant.core.event;

import java.util.ArrayList;
import java.util.Collection;

import com.variant.core.VariantSession;
import com.variant.core.schema.Test;
import com.variant.core.schema.View;


public class ViewServeEvent extends BaseEvent {

	private static final String EVENT_NAME = "VIEW_SERVE";
	
	private View view;
	private String viewResolvedPath;
	
	/**
	 * New constructor
	 */
	public ViewServeEvent(View view, VariantSession session, Status status, String viewResolvedPath) {
		super(session, EVENT_NAME, view.getName(), status);
		this.view = view;
		this.viewResolvedPath = viewResolvedPath;
	}
	
	/**
     *
	 */
	@Override
	public Collection<EventExperience> getEventExperiences() {
		
		Collection<EventExperience> result = new ArrayList<EventExperience>();
		
		for (Test.Experience exp: experiences) {
			result.add(new EventExperience(this.getId(), exp, view, viewResolvedPath));
		}
		
		return result;
	}

		
}
