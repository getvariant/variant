package com.variant.core.schema.impl;

import static com.variant.core.RuntimeError.STATE_NOT_INSTRUMENTED_BY_TEST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.variant.core.CoreException;
import com.variant.core.schema.Hook;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * 
 * @author Igor
 *
 */
public class StateImpl implements State {

	private final Schema schema;
	private final String name;
	private final LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>();
    
	// Hooks are keyed by name.
	private LinkedHashSet<Hook> hooks = new LinkedHashSet<Hook>();

	/**
	 * Package scoped constructor;
	 * @param name
	 * @param path
	 */
	public StateImpl(Schema schema, String name) {
		this.schema = schema;
		this.name = name;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getParameter(String name) {
		return parameters.get(name);
	}

	@Override
	public List<Test> getInstrumentedTests() {
		
		ArrayList<Test> result = new ArrayList<Test>();
		
		for (Test test: schema.getTests()) {
			for (Test.OnState tov: test.getOnStates()) {
				if (tov.getState().equals(this)) result.add(test);
			}
		}
		return Collections.unmodifiableList(result);
	}

	@Override
	public boolean isInstrumentedBy(Test test) {

		for (Test.OnState tov: test.getOnStates()) {
			if (tov.getState().equals(this)) return true;
		}
		return false;
	}		

	@Override
	public boolean isNonvariantIn(Test test) {

		for (Test.OnState tov: test.getOnStates()) {
			if (tov.getState().equals(this)) return tov.isNonvariant();
		}
		throw new CoreException.User(STATE_NOT_INSTRUMENTED_BY_TEST, name, test.getName());
	}

	/**
	 * 
	 */
	@Override
	public List<Hook> getHooks() {
		ArrayList<Hook> result = new ArrayList<Hook>(hooks.size());
		result.addAll(hooks);
		return Collections.unmodifiableList(result);
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//
	/**
	 * 
	 * @param parameters
	 */
	public void setParameterMap(Map<String, String> params) {
		this.parameters.putAll(params);
	}
	
	/**
	 * Entire state param map
	 */
	public Map<String, String> getParameterMap() {
		return parameters;
	}
	
	/**
	 * Add user hook to this test
	 * @param hook
	 * @return true if hook didn't exist, false if did.
	 */
	public boolean addHook(Hook hook) {
		return hooks.add(hook);
	}
	
	/**
	 * States are held in a HashSet, keyed by view name.
	 */
	@Override
	public boolean equals(Object other) {
		if (! (other instanceof State)) return false;
		return ((State) other).getName().equals(this.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
