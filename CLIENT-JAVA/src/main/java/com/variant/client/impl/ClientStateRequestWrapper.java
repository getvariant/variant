package com.variant.client.impl;
/*
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.variant.client.VariantTargetingTracker;
import com.variant.core.VariantCoreSession;
import com.variant.core.VariantCoreStateRequest;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class ClientStateRequestWrapper implements VariantCoreStateRequest {

	private VariantCoreStateRequest variantRequest;
	private VariantCoreSession clientSession;
	
	public ClientStateRequestWrapper(
			VariantCoreSession clientSession, VariantCoreStateRequest variantRequest) 
	{
		this.variantRequest = variantRequest;
		this.clientSession = clientSession;
	}

	// Extra Methods WE SNOULD NOT NEED THIS
	//public HttpServletRequest getHttpServletRequest() {
	//	return (HttpServletRequest) userData[0];
	//}

	public VariantCoreStateRequest getOriginalRequest() {
		return variantRequest;
	}
	
	/// Pass-through methods ///
	@Override
	public VariantCoreSession getSession() {
		return clientSession;
	}

	@Override
	public State getState() {
		return variantRequest.getState();
	}

	@Override
	public Map<String, String> getResolvedParameterMap() {
		return variantRequest.getResolvedParameterMap();
	}
	
	@Override
	public Collection<Experience> getTargetedExperiences() {
		return variantRequest.getTargetedExperiences();
	}

	@Override
	public Experience getTargetedExperience(Test test) {
		return variantRequest.getTargetedExperience(test);
	}

	@Override
	public void setStatus(Status status) {
		variantRequest.setStatus(status);
	}

	@Override
	public VariantEvent getStateVisitedEvent() {
		return variantRequest.getStateVisitedEvent();
	}

	@Override
	public Status getStatus() {
		return variantRequest.getStatus();
	}

	@Override
	public void commit(Object... userData) {
		variantRequest.commit(userData);
	}

	@Override
	public void commit() {
		// TODO Auto-generated method stub
		
	}

}
*/