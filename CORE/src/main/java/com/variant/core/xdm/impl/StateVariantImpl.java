package com.variant.core.xdm.impl;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.event.impl.util.CaseInsensitiveMap;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.xdm.State;
import com.variant.core.xdm.StateVariant;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;
import com.variant.core.xdm.Test.OnState;


class StateVariantImpl implements StateVariant {

	private TestOnStateImpl onStateImpl;
	private TestExperienceImpl ownExperience;
	private List<TestExperienceImpl> covarExperiences;
	private Map<String,String> params;
	
	/**
	 * @param onView
	 * @param experiences
	 * @param path
	 */
	StateVariantImpl(TestOnStateImpl onViewImpl, TestExperienceImpl ownExperience, List<TestExperienceImpl> covarExperiences, Map<String,String> params) {
		this.onStateImpl = onViewImpl;
		this.ownExperience = ownExperience;
		this.covarExperiences = covarExperiences;
		this.params = new CaseInsensitiveMap<String>(params);

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
	public Map<String, String> getParameterMap() {
		return params;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Experience> getCovariantExperiences() {
		return (List<Experience>) (List<?>) Collections.unmodifiableList(covarExperiences);
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
	@Override
	public String toString() {
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
			throw new VariantInternalException("Unable to serialize object [" + this.getClass().getSimpleName() + "]", e);
		}

	}
}
