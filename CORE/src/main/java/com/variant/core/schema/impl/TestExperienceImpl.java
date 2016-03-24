package com.variant.core.schema.impl;

import com.variant.core.schema.Test;

// Remove public modifier is the result of exposing the server side
// constructor.
public class TestExperienceImpl implements Test.Experience  {

	private String name;
	private Test test;
	private Number weight;
	boolean isControl;
		
	/**
	 * Instantiation.
	 * @param name
	 */
	TestExperienceImpl(String name, Number weight, boolean isControl) {
		this.name = name;
		this.weight = weight;
		this.isControl = isControl;
	}
	
	/**
	 * Test is unknown at the time of instantiation.
	 * @param test
	 */
	void setTest(Test test) {
		this.test = test;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	public String getName() {
		return name;
	}

	public Test getTest() {
		return test;
	}

	public boolean isControl() {
		return isControl;
	}

	public Number getWeight() {
		return weight;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TestExperienceImpl)) return false;
		TestExperienceImpl other = (TestExperienceImpl) o;
		return test.equals(other.test) && name.equals(other.name);
	}
	
	@Override
	public int hashCode() {
		return test.hashCode() + name.hashCode();
	}
	
	@Override
	public String toString() {
		return test.getName() + "." + name;
	}
}
