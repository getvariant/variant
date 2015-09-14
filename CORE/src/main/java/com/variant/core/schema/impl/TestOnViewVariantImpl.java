package com.variant.core.schema.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnState;


class TestOnViewVariantImpl implements Test.OnState.Variant{

	private TestOnStateImpl onViewImpl;
	private TestExperienceImpl ownExperience;
	private List<TestExperienceImpl> covarExperiences;
	private Map<String,String> params;
	
	/**
	 * @param onView
	 * @param experiences
	 * @param path
	 */
	TestOnViewVariantImpl(TestOnStateImpl onViewImpl, TestExperienceImpl ownExperience, List<TestExperienceImpl> covarExperiences, Map<String,String> params) {
		this.onViewImpl = onViewImpl;
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
	 * 
	 * @return
	 */
	@Override
	public OnState getOnState() {
		return onViewImpl;
	}

	/**
	 * 
	 */
	@Override
	public Test getTest() {
		return onViewImpl.getTest();
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

}
