package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.variant.core.Variant;
import com.variant.core.VariantInternalException;
import com.variant.core.schema.Test;

/**
 * 
 * @author Igor
 *
 */
public class TestImpl implements Test {

	// As defined:
	private String name;
	private List<TestImpl> covariantTests;
	private List<TestExperienceImpl> experiences;
	private List<TestOnViewImpl> onViews;
	
	// Computed and cached:
	private List<TestImpl> fullCovariantList = null;
	
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
	
	/**
	 * 
	 * @param tests
	 */
	void setCovariantTests(List<TestImpl> tests) {
		this.covariantTests = tests;
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

	/**
	 * We can (because the result won't change in the lifetime of the current schema, i.e.
	 * of this object) and should (because we don't want to generate a new heap object each
	 * time client calls, in case client forgets to release them.
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Test> getCovariantTests() {
		
		// 1. All of this tests
		if (fullCovariantList == null) { 
			List<TestImpl> result = new ArrayList<TestImpl>();
			result.addAll(covariantTests);
			// 2. Any other test whose covariance list contains this test.
			for (Test other: Variant.getSchema().getTests()) {
				if (other.equals(this)) continue;
				for (TestImpl otherCovar: ((TestImpl) other).covariantTests) {
					if (otherCovar.equals(this)) {
						result.add((TestImpl) other);
						break;
					}
				}
			}

			fullCovariantList = result;
		}
		
		return (List<Test>) (List<?>) Collections.unmodifiableList(fullCovariantList);
	}

}

