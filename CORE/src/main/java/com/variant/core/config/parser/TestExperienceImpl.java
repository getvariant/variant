package com.variant.core.config.parser;

import com.variant.core.config.Test;

class TestExperienceImpl implements Test.Experience  {

	private String name;
	private Test test;
	private float weight;
	boolean isControl;
	
	/**
	 * 
	 * @param name
	 */
	TestExperienceImpl(String name, float weight, boolean isControl) {
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

	public float getWeight() {
		return weight;
	}

}
