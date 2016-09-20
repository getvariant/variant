package com.variant.core.xdm.impl;

import java.util.HashSet;
import java.util.Set;

import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;

// Remove public modifier is the result of exposing the server side
// constructor.
public class TestExperienceImpl implements Test.Experience  {

	private String name;
	private Test test;
	private Number weight;
	private boolean isControl;
	private Set<State> uninstrumentedStates = new HashSet<State>();
	
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

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Test getTest() {
		return test;
	}

	@Override
	public boolean isControl() {
		return isControl;
	}

	@Override
	public Number getWeight() {
		return weight;
	}
	
	@Override
	public boolean isDefinedOn(State state) {
		if (state == null) throw new NullPointerException("Null state");
		return !uninstrumentedStates.contains(state);
	}
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public void addUninstrumentedState(State state) {
		uninstrumentedStates.add(state);
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
