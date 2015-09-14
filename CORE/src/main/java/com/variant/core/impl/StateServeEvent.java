package com.variant.core.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.variant.core.VariantEventExperience;
import com.variant.core.event.VariantEventSupport;
import com.variant.core.schema.Test;


public class StateServeEvent extends VariantEventSupport {

	private static final String EVENT_NAME = "VIEW_SERVE";
	private static final String PARAM_NAME_REQUEST_STATUS = "VIEW_REQUEST_STAUTS";
	
	private VariantViewRequestImpl request;
	
	/**
	 * New constructor
	 */
	StateServeEvent(VariantViewRequestImpl request, Map<String,String> resolvedParameters) {
		super(request, EVENT_NAME, request.getState().getName());
		this.request = request;
		for (Map.Entry<String, String> param: resolvedParameters.entrySet()) {
			setParameter(param.getKey(), param.getValue());
			
		}
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
			result.add(new StateServeEventExperience(this, exp));
		}
		
		return result;
	}

		
}
