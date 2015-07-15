package com.variant.core.schema.impl;

import java.util.Collections;
import java.util.List;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnView;


class TestOnViewVariantImpl implements Test.OnView.Variant{

	private TestOnViewImpl onViewImpl;
	private TestExperienceImpl ownExperience;
	private List<TestExperienceImpl> covarExperiences;
	private String path;
	
	/**
	 * @param onView
	 * @param experiences
	 * @param path
	 */
	TestOnViewVariantImpl(TestOnViewImpl onViewImpl, TestExperienceImpl ownExperience, List<TestExperienceImpl> covarExperiences, String path) {
		this.onViewImpl = onViewImpl;
		this.ownExperience = ownExperience;
		this.covarExperiences = covarExperiences;
		this.path = path;
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
	public OnView getOnView() {
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
	public String getPath() {
		return path;
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
