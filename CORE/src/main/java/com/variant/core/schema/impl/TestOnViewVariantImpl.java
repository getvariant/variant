package com.variant.core.schema.impl;

import java.util.Collections;
import java.util.List;

import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.OnView;


class TestOnViewVariantImpl implements Test.OnView.Variant{

	private TestOnViewImpl onViewImpl;
	private List<TestExperienceImpl> experiences;
	private String path;
	
	/**
	 * @param onView
	 * @param experiences
	 * @param path
	 */
	TestOnViewVariantImpl(TestOnViewImpl onViewImpl, List<TestExperienceImpl> experiences, String path) {
		this.onViewImpl = onViewImpl;
		this.experiences = experiences;
		this.path = path;
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
	public List<Experience> getExperiences() {
		return (List<Experience>) (List<?>) Collections.unmodifiableList(experiences);
	}

	/**
	 * 
	 */
	@Override
	public Experience getLocalExperience() {
		for (Experience e: experiences) {
			if (e.getTest().equals(getTest())) return e;
		}
		return null;
	}
}
