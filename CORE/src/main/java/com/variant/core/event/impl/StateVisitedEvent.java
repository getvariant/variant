package com.variant.core.event.impl;

import java.io.Serializable;
import java.util.Date;

import com.variant.core.schema.State;


public class StateVisitedEvent extends VariantEventSupport implements Serializable {

	/**
	 */
	private static final long serialVersionUID = 1L;

	public static final String EVENT_NAME = "STATE_VISIT";
	
	private State state;
	private Date createDate = new Date();
	
	/**
	 * 
	 * @param state
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
