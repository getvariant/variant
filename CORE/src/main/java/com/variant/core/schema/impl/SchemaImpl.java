package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * @author Igor
 */
public class SchemaImpl implements Schema {

	// Views are keyed by name
	private LinkedHashSet<State> states = new LinkedHashSet<State>();
	
	// Tests are keyed by name
	private LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
		
	/**
	 * Package instantiation.
	 */
	SchemaImpl() {}
	
	/**
	 * Add state to the set.
	 * @param state
	 * @return true if element didn't exist, false if did.
	 */
	boolean addState(State state) {
		return states.add(state);
	}

	/**
	 * Add test to the set.
	 * @param test
	 * @return true if element didn't exist, false if did.
	 */
	boolean addTest(Test test) {
		return tests.add(test);
	}
		
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//

	/**
	 * States in the ordinal order, as an immutable list.
	 */
	@Override
	public List<State> getStates() {
		ArrayList<State> result = new ArrayList<State>(states.size());
		result.addAll(states);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get a state by name
	 */
	@Override
	public State getState(String name) {
		for (State state: states) {
			if (state.getName().equals(name)) return state;
		}
		return null;
	}

	/**
	 * Get all tests.
	 */
	@Override
	public List<Test> getTests() {
		ArrayList<Test> result = new ArrayList<Test>(tests.size());
		for (Test test: tests) {
			result.add(test);
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Gat a test by name.
	 */
	@Override
	public Test getTest(String name) {
		for (Test test: tests) {
			if (test.getName().equals(name)) return test;
		}
		return null;
	}
}
