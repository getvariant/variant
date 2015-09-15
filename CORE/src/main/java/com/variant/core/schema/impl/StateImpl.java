package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.variant.core.Variant;
import com.variant.core.exception.VariantRuntimeException;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.MessageTemplate;

/**
 * 
 * @author Igor
 *
 */
public class StateImpl implements State {

	private String name;
	private Map<String,String> parameters;

	/**
	 * Package scoped constructor;
	 * @param name
	 * @param path
	 */
	StateImpl(String name, Map<String, String> parameters) {
		this.name = name;
		this.parameters = parameters;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Map<String, String> getParameterMap() {
		return Collections.unmodifiableMap(parameters);
	}

	@Override
	public String getParameter(String name) {
		return parameters.get(name);
	}

	@Override
	public List<Test> getInstrumentedTests() {
		
		ArrayList<Test> result = new ArrayList<Test>();
		
		for (Test test: Variant.Factory.getInstance().getSchema().getTests()) {
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
		throw new VariantRuntimeException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, name, test.getName());
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

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

}
