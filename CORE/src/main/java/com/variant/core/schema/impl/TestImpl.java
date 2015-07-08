package com.variant.core.schema.impl;

import java.util.Collections;
import java.util.List;

import com.variant.core.VariantInternalException;
import com.variant.core.schema.Test;

/**
 * 
 * @author Igor
 *
 */
public class TestImpl implements Test {

	private String name;
	private List<TestExperienceImpl> experiences;
	private List<TestOnViewImpl> onViews;
	
	/**
	 * 
	 * @param name
	 */
	TestImpl(String name) {
		this.name = name;
	}
	
	/**
	 * 
	 * @param experiences
	 */
	void setExperiences(List<TestExperienceImpl> experiences) {
		this.experiences = experiences;
	}
	
	/**
	 * 
	 * @param onViews
	 */
	void setOnViews(List<TestOnViewImpl> onViews) {
		this.onViews = onViews;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//
	
	/**
	 * View's declared name
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Experience> getExperiences() {
		return  (List<Experience>) (List<?>) Collections.unmodifiableList(experiences);
	}

	/**
	 * 
	 */
	@Override
	public Experience getExperience(String name) {
		for (TestExperienceImpl e: experiences) {
			if (e.getName().equalsIgnoreCase(name)) return e;
		}
		return null;
	}
	
	/**
	 * 
	 */
	@Override
	public Experience getControlExperience() {
		for (TestExperienceImpl e: experiences) {
			if (e.isControl()) return e;
		}
		throw new VariantInternalException("No control experience found in test [' + getName() + ']");
	}

	/**
	 * 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Test.OnView> getOnViews() {
		return (List<Test.OnView>) (List<?>) Collections.unmodifiableList(onViews);
	}

	/**
	 * Tests are held in a HashSet, keyed by test name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof Test)) return false;
		return ((Test) other).getName().equalsIgnoreCase(this.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}

