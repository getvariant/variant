package com.variant.core.schema.impl;

import com.variant.core.config.ComptimeService;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.schema.Test;

// Remove public modifier is the result of exposing the server side
// constructor.
public class TestExperienceImpl implements Test.Experience  {

	private String name;
	private Test test;
	private Number weight;
	boolean isControl;
	
	// Transitional hack until server has schema
	private String testName;
	
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
	 * Server side constructor only.
	 * Transitional hack to avoid instantiating the fully fledged object becase we don't
	 * yet have schema on server.  Remove public modifier from the class when removing this.
	 * 
	 * @param testName
	 * @param name
	 * @param isControl
	 */
	public TestExperienceImpl(String testName, String name, boolean isControl) {
		if (!ComptimeService.getComponent().equals("Server"))
			throw new VariantInternalException("Operation is supported only on Server");
		this.testName = testName;
		this.name = name;
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

	//---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	public String getTestName() {
		if (!ComptimeService.getComponent().equals("Server"))
			throw new VariantInternalException("Operation is supported only on Server");
		return testName;
	}
}
