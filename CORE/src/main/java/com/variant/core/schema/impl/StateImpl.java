package com.variant.core.schema.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.variant.core.event.impl.util.CaseInsensitiveMap;
import com.variant.core.event.impl.util.CaseInsensitiveUnmodifiableMap;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.schema.Schema;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;

/**
 * 
 * @author Igor
 *
 */
public class StateImpl implements State {

	private Schema schema;
	private String name;
	private CaseInsensitiveMap<String> parameters;

	/**
	 * Package scoped constructor;
	 * @param name
	 * @param path
	 */
	StateImpl(Schema schema, String name, Map<String, String> parameters) {
		this.schema = schema;
		this.name = name;
		this.parameters = new CaseInsensitiveMap<String>(parameters);
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
	public Map<String, String> getParameterMap() {
		return new CaseInsensitiveUnmodifiableMap<String>(parameters);
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
		throw new VariantRuntimeUserErrorException(MessageTemplate.RUN_STATE_NOT_INSTRUMENTED_FOR_TEST, name, test.getName());
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

	@Override
	public String toString() {
		return name;
	}
}
