package com.variant.core.svrstub;

import com.variant.core.exception.RuntimeInternalException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * Server side stub.
 * Transitional hack to avoid instantiating the fully fledged object since we don't
 * yet have schema on server. 
 */
public class TestExperienceServerStub implements Test.Experience  {

	private String name;
	private Test test;
	boolean isControl;
		
	public TestExperienceServerStub(String testName, String name, boolean isControl) {
		this.test = new TestServerStub(testName);
		this.name = name;
		this.isControl = isControl;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Test getTest() {
		return test;
	}

	@Override
	public Number getWeight() {
		throw new RuntimeInternalException("Method not supported");
	}

	@Override
	public boolean isControl() {
		return isControl;
	}

	@Override
	public boolean isDefinedOn(State state) {
		throw new RuntimeInternalException("Method not supported");
	}
	
	
}
