package com.variant.core.schema.impl;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.exception.CoreException;
import com.variant.core.schema.State;
import com.variant.core.schema.StateVariant;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState;


public class StateVariantImpl implements StateVariant {

	private TestOnStateImpl onStateImpl;
	private TestExperienceImpl ownExperience;
	private List<TestExperienceImpl> covarExperiences;
	private Map<String,String> params;
	
	/**
	 * @param onView
	 * @param experiences
	 * @param path
	 */
	public StateVariantImpl(TestOnStateImpl onViewImpl, TestExperienceImpl ownExperience, List<TestExperienceImpl> covarExperiences, Map<String,String> params) {
		this.onStateImpl = onViewImpl;
		this.ownExperience = ownExperience;
		this.covarExperiences = covarExperiences;
		this.params = params;
	}

	/**
	 * 
	 * @param experience
	 */
	void addCovariantExperience(TestExperienceImpl experience) {
		covarExperiences.add(experience);
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 */
	@Override
	public OnState getOnState() {
		return onStateImpl;
	}

	/**
	 */
	@Override
	public State getState() {
		return onStateImpl.getState();
	}
	
	/**
	 */
	@Override
	public Test getTest() {
		return onStateImpl.getTest();
	}

	/**
	 */
	@Override
	public String getParameter(String name) {
		return params.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Experience> getCovariantExperiences() {
		if (covarExperiences == null) return null;
		return (List<Experience>) (List<?>) Collections.unmodifiableList(covarExperiences);
	}

	@Override
	public boolean isProper() {
		return covarExperiences == null;
	}
	
	/**
	 * 
	 */
	@Override
	public Experience getExperience() {
		return ownExperience;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	public Map<String, String> getParameters() {
		return params;
	}
	
	public String toJson() {
		try {
			StringWriter result = new StringWriter(2048);
			JsonGenerator jsonGen = new JsonFactory().createGenerator(result);
			jsonGen.writeStartObject();
			jsonGen.writeStringField("state", onStateImpl.getState().getName());
			jsonGen.writeStringField("test", onStateImpl.getTest().getName());
			jsonGen.writeStringField("params", params.toString());
			jsonGen.writeEndObject();
			jsonGen.flush();
			return result.toString();
		}
		catch (Exception e) {
			throw new CoreException.Internal("Unable to serialize object [" + getClass().getSimpleName() + "]", e);
		}

	}
}
