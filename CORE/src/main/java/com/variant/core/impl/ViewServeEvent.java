package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.variant.core.VariantEventExperience;
import com.variant.core.event.VariantEventSupport;
import com.variant.core.schema.Test;


public class ViewServeEvent extends VariantEventSupport {

	private static final String EVENT_NAME = "VIEW_SERVE";
	private static final String PARAM_NAME_VIEW_RESOLVED_PATH = "VIEW_RESOLVED_PATH";
	private static final String PARAM_NAME_REQUEST_STATUS = "VIEW_REQUEST_STAUTS";
	
	private VariantViewRequestImpl request;
	
	/**
	 * New constructor
	 */
	ViewServeEvent(VariantViewRequestImpl request, String viewResolvedPath) {
		super(request, EVENT_NAME, request.getView().getName());
		this.request = request;
		setParameter(PARAM_NAME_VIEW_RESOLVED_PATH, viewResolvedPath);
		setParameter(PARAM_NAME_REQUEST_STATUS, request.getStatus().ordinal());
	}
	
	/**
	 * @return
	 */
	public VariantViewRequestImpl getViewRequest() {
		return request;
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
		
		return result;
	}

		
}
