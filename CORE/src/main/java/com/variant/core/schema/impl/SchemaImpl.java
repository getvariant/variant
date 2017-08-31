package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Hook;
import com.variant.core.util.VariantStringUtils;

/**
 * @author Igor
 */
public class SchemaImpl implements Schema {

	private final String ID = VariantStringUtils.random64BitString(new Random(System.currentTimeMillis()));
	
	// Meta
	private String name = null;
	private String comment = null;
	
	// Hooks are keyed by name.
	private LinkedHashSet<Hook> hooks = new LinkedHashSet<Hook>();
	
	// Views are keyed by name
	private LinkedHashSet<State> states = new LinkedHashSet<State>();
	
	// Tests are keyed by name
	private LinkedHashSet<Test> tests = new LinkedHashSet<Test>();
		
	/**
	 * Package instantiation.
	 */
	public SchemaImpl() {}
					
    //---------------------------------------------------------------------------------------------//
	//                                    PUBLIC INTERFACE                                         //
	//---------------------------------------------------------------------------------------------//	

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public List<Hook> getHooks() {
		ArrayList<Hook> result = new ArrayList<Hook>(hooks.size());
		result.addAll(hooks);
		return Collections.unmodifiableList(result);
	}

	@Override
	public String getId() {
		return ID;
	}
	
	/**
	 * States in the ordinal order, as an immutable list.
	 */
	@Override
	public List<State> getStates() {
		//isUsable();
		ArrayList<State> result = new ArrayList<State>(states.size());
		result.addAll(states);
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get a state by name
	 */
	@Override
	public State getState(String name) {
		//isUsable();
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
		//isUsable();
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
		//isUsable();
		for (Test test: tests) {
			if (test.getName().equals(name)) return test;
		}
		return null;
	}
	
    //---------------------------------------------------------------------------------------------//
	//                                       PUBLIC EXT                                            //
	//---------------------------------------------------------------------------------------------//

	/**
	 * Set schema's meta data.
	 * @param name
	 * @param comment
	 */
	public void setMeta(String name, String comment) {
		this.name = name;
		this.comment = comment;
	}
	
	/**
	 * Add state to this schema.
	 * @param state
	 * @return true if state didn't exist, false if did.
	 */
	public boolean addState(State state) {
		return states.add(state);
	}

	/**
	 * Add test to this schema
	 * @param test
	 * @return true if test didn't exist, false if did.
	 */
	public boolean addTest(Test test) {
		return tests.add(test);
	}

	/**
	 * Add user hook to this schema
	 * @param hook
	 * @return true if hook didn't exist, false if did.
	 */
	public boolean addHook(Hook hook) {
		return hooks.add(hook);
	}

}
