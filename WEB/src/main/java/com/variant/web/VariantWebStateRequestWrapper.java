package com.variant.web;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.VariantTargetingTracker;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

class VariantWebStateRequestWrapper implements VariantStateRequest {

	private VariantStateRequest variantRequest;
	private HttpServletRequest httpRequest;
	
	VariantWebStateRequestWrapper(VariantStateRequest variantRequest, HttpServletRequest httpRequest) {
		this.variantRequest = variantRequest;
		this.httpRequest = httpRequest;
	}

	/// Extra Methods ///
	HttpServletRequest getHttpServletRequest() {
		return httpRequest;
	}
	
	VariantStateRequest getOriginalRequest() {
		return variantRequest;
	}
	
	/// Pass-through methods ///
	@Override
	public VariantSession getSession() {
		return variantRequest.getSession();
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
	public VariantTargetingTracker getTargetingTracker() {
		return variantRequest.getTargetingTracker();
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
	public Collection<Test> getDisqualifiedTests() {
		return variantRequest.getDisqualifiedTests();
	}

	@Override
	public void setStatus(Status status) {
		variantRequest.setStatus(status);
	}

	@Override
	public Collection<VariantEvent> getPendingEvents() {
		return variantRequest.getPendingEvents();
	}
}
