package com.variant.core.schema.impl;

import java.util.HashSet;
import java.util.Set;

import com.variant.core.schema.State;
import com.variant.core.schema.Variation;

// Remove public modifier is the result of exposing the server side
// constructor.
public class TestExperienceImpl implements Variation.Experience  {

	private String name;
	private Variation test;
	private Number weight;
	private boolean isControl;
	private Set<State> uninstrumentedStates = new HashSet<State>();
	
	/**
	 * Instantiation.
	 * @param name
	 */
	public TestExperienceImpl(String name, Number weight, boolean isControl) {
		this.name = name;
		this.weight = weight;
		this.isControl = isControl;
	}
		
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Variation getTest() {
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
	public boolean isPhantom(State state) {
		if (state == null) throw new NullPointerException("Null state");
		return uninstrumentedStates.contains(state);
	}
	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public void setTest(Variation test) {
		this.test = test;
	}

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
