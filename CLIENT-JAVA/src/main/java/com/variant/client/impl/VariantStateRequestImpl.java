package com.variant.client.impl;

import java.util.Collection;
import java.util.Map;

import com.variant.client.VariantStateRequest;
import com.variant.core.VariantCoreSession;
import com.variant.core.event.VariantEvent;
import com.variant.core.impl.VariantCoreStateRequestImpl;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class VariantStateRequestImpl implements VariantStateRequest {

	private VariantCoreStateRequestImpl coreStateRequest;
	
	public VariantStateRequestImpl(VariantCoreStateRequestImpl coreStateRequest) {
		this.coreStateRequest = coreStateRequest;
	}

	@Override
	public VariantCoreSession getSession() {
		return coreStateRequest.getSession();
	}

	@Override
	public State getState() {
		return coreStateRequest.getState();
	}

	@Override
	public Map<String, String> getResolvedParameterMap() {
		return coreStateRequest.getResolvedParameterMap();
	}

	@Override
	public Collection<Experience> getTargetedExperiences() {
		// TODO Auto-generated method stub
		return coreStateRequest.getTargetedExperiences();
	}

	@Override
	public Experience getTargetedExperience(Test test) {
		return coreStateRequest.getTargetedExperience(test);
	}

	@Override
	public VariantEvent getStateVisitedEvent() {
		return coreStateRequest.getStateVisitedEvent();
	}

	@Override
	public void setStatus(Status status) {
		coreStateRequest.setStatus(status);
	}

	@Override
	public void commit(Object... userData) {
		coreStateRequest.commit(userData);
	}

	@Override
	public Status getStatus() {
		return coreStateRequest.getStatus();
	}
}
