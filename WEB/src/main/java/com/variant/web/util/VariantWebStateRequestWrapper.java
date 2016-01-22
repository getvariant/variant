package com.variant.web.util;

import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.Predicate;

import com.variant.core.VariantSession;
import com.variant.core.VariantStateRequest;
import com.variant.core.VariantTargetingTracker;
import com.variant.core.event.VariantEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class VariantWebStateRequestWrapper implements VariantStateRequest {

	private VariantStateRequest variantRequest;
	private HttpServletRequest httpRequest;
	
	public VariantWebStateRequestWrapper(VariantStateRequest variantRequest, HttpServletRequest httpRequest) {
		this.variantRequest = variantRequest;
		this.httpRequest = httpRequest;
	}

	/// Extra Methods ///
	public HttpServletRequest getHttpServletRequest() {
		return httpRequest;
	}
	
	public VariantStateRequest getOriginalRequest() {
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

	@Override
	public Collection<VariantEvent> getPendingEvents(Predicate<VariantEvent> filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Status getStatus() {
		return variantRequest.getStatus();
	}

	@Override
	public void triggerEvent(VariantEvent event) {
		// TODO Auto-generated method stub
	}
}
