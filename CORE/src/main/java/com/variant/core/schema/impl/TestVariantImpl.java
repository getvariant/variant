package com.variant.core.schema.impl;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState;
import com.variant.core.util.CaseInsensitiveMap;


class TestOnViewVariantImpl implements Test.OnState.Variant{

	private TestOnStateImpl onStateImpl;
	private TestExperienceImpl ownExperience;
	private List<TestExperienceImpl> covarExperiences;
	private Map<String,String> params;
	
	/**
	 * @param onView
	 * @param experiences
	 * @param path
	 */
	TestOnViewVariantImpl(TestOnStateImpl onViewImpl, TestExperienceImpl ownExperience, List<TestExperienceImpl> covarExperiences, Map<String,String> params) {
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
	 * 
	 * @return
	 */
	@Override
	public OnState getOnState() {
		return onStateImpl;
	}

	/**
	 * 
	 */
	@Override
	public Test getTest() {
		return onStateImpl.getTest();
	}
	
	/**
	 * 
	 * @return
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
