package com.variant.core.event.impl;

import java.io.Serializable;
import java.util.Date;

import com.variant.core.schema.State;


public class StateVisitedEvent extends VariantEventSupport implements Serializable {

	/**
	 */
	private static final long serialVersionUID = 1L;

	public static final String EVENT_NAME = "STATE_SERVE";
	
	private State state;
	private Date createDate = new Date();
	
	/**
	 * New constructor
	 *
	public StateVisitedEvent(VariantStateRequestImpl request, Map<String,String> resolvedParameters) {
		super(EVENT_NAME, request.getState().getName(), request);
		this.request = request;
		for (Map.Entry<String, String> param: resolvedParameters.entrySet()) {
			setParameter(param.getKey(), param.getValue());
			
		}
	}
	*/

	public StateVisitedEvent(State state) {
		this.state = state;
	}
	
	@Override
	public String getEventName() {
		return EVENT_NAME;
	}

	@Override
	public String getEventValue() {
		return state.getName();
	}

	@Override
	public Date getCreateDate() {
		return createDate;
	}

}
