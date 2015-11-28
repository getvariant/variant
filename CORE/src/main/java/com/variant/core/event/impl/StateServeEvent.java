package com.variant.core.event.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.variant.core.event.VariantEventExperience;
import com.variant.core.impl.VariantStateRequestImpl;
import com.variant.core.schema.Test;


public class StateServeEvent extends VariantEventSupport {

	private static final String EVENT_NAME = "VIEW_SERVE";
	private static final String PARAM_NAME_REQUEST_STATUS = "VIEW_REQUEST_STAUTS";
	
	private VariantStateRequestImpl request;
	
	/**
	 * New constructor
	 */
	public StateServeEvent(VariantStateRequestImpl request, Map<String,String> resolvedParameters) {
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
	public VariantStateRequestImpl getViewRequest() {
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
