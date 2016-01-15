package com.variant.core.event.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.variant.core.event.VariantEventVariant;
import com.variant.core.impl.VariantStateRequestImpl;
import com.variant.core.schema.Test;


public class StateServeEvent extends VariantEventSupport {

	public static final String EVENT_NAME = "STATE_SERVE";
	
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
	}
	
	/**
	 * @return
	 */
	public VariantStateRequestImpl getStateRequest() {
		return request;
	}
	
	/**
     *
	 */
	@Override
	public Collection<VariantEventVariant> getEventVariants() {
		
		Collection<VariantEventVariant> result = new ArrayList<VariantEventVariant>();
		
		for (Test.Experience exp: experiences) {
			result.add(new StateServeEventVariant(this, exp));
		}
		
		return result;
	}

		
}
