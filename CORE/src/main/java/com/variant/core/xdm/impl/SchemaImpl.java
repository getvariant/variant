package com.variant.core.xdm.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.xdm.Schema;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;

/**
 * @author Igor
 */
public class SchemaImpl implements Schema {

	private final String ID = VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()));
	
	// Views are keyed by name
	private LinkedHashSet<State> states = new LinkedHashSet<State>();
	
	// Tests are keyed by name
	private LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
	
	// Life cycle.
	private InternalState state = InternalState.NEW;
	
	/**
	 * Package instantiation.
	 */
	SchemaImpl() {}
	
	/**
	 * Public ops are only allowed on a currently deployed schema.
	 */
	private void checkInternalState() {
		if (state == InternalState.UNDEPLOYED)
			throw new VariantRuntimeUserErrorException(MessageTemplate.RUN_SCHEMA_OBSOLETE, ID);
		else if (state == InternalState.FAILED)
			throw new VariantInternalException("Called on a FAILED schema");
	}
	
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
	 * 
	 */
	@Override
	public String getId() {
		return ID;
	}
	
	/**
	 * States in the ordinal order, as an immutable list.
	 */
	@Override
	public List<State> getStates() {
		checkInternalState();
		ArrayList<State> result = new ArrayList<State>(states.size());
		result.addAll(states);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get a state by name
	 */
	@Override
	public State getState(String name) {
		checkInternalState();
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
		checkInternalState();
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
		checkInternalState();
		for (Test test: tests) {
			if (test.getName().equals(name)) return test;
		}
		return null;
	}
	
    //---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	public enum InternalState {NEW, FAILED, DEPLOYED, UNDEPLOYED}

	/**
	 * Set internal state
	 * @param state
	 */
	public void setInternalState(InternalState state) {
		this.state = state;
	}

}
