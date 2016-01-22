package com.variant.core.event.impl;

import java.util.Map;

import com.variant.core.impl.VariantStateRequestImpl;


public class StateServeEvent extends VariantEventSupport {

	public static final String EVENT_NAME = "STATE_SERVE";
	
	private VariantStateRequestImpl request;
	
	/**
	 * New constructor
	 */
	public StateServeEvent(VariantStateRequestImpl request, Map<String,String> resolvedParameters) {
		super(EVENT_NAME, request.getState().getName(), request);
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
	 *
	@Override
	public Collection<EventVariant> getEventVariants() {
		
		Collection<EventVariant> result = new ArrayList<EventVariant>();
		
		for (Test.Experience exp: experiences) {
			result.add(new StateServeEventVariant(this, exp));
		}
		
		return result;
	}
	*/
		
}
