package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import com.variant.core.event.impl.util.VariantStringUtils;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * @author Igor
 */
public abstract class SchemaImpl implements Schema {

	private final String ID = VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()));
	
	// Views are keyed by name
	private LinkedHashSet<State> states = new LinkedHashSet<State>();
	
	// Tests are keyed by name
	private LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
	
	// Life cycle.
	// private InternalState state = InternalState.NEW;
	
	/**
	 * Package instantiation.
	 */
	public SchemaImpl() {}
			
	/**
	 * extensions will provide implementaitons.
	 */  
	abstract protected void isUsable();
/* CLEANUP this should move to client
		if (state == InternalState.UNDEPLOYED)
			throw new VariantRuntimeUserErrorException(Error.RUN_SCHEMA_OBSOLETE, ID);
		else if (state == InternalState.FAILED)
			throw new RuntimeInternalException("Called on a FAILED schema");
	}
*/
	
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
		isUsable();
		ArrayList<State> result = new ArrayList<State>(states.size());
		result.addAll(states);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get a state by name
	 */
	@Override
	public State getState(String name) {
		isUsable();
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
		isUsable();
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
		isUsable();
		for (Test test: tests) {
			if (test.getName().equals(name)) return test;
		}
		return null;
	}
	
    //---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Add state to this schema.
	 * @param state
	 * @return true if element didn't exist, false if did.
	 */
	public boolean addState(State state) {
		return states.add(state);
	}

	/**
	 * Add test to this schema
	 * @param test
	 * @return true if element didn't exist, false if did.
	 */
	public boolean addTest(Test test) {
		return tests.add(test);
	}

	//public enum InternalState {NEW, FAILED, DEPLOYED, UNDEPLOYED}

	/* CLEANUP
	 * Set internal state
	 * @param state
	 *
	public void setInternalState(InternalState state) {
		this.state = state;
	}
    */
}
