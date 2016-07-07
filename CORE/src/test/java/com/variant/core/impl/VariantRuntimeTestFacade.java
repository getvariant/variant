package com.variant.core.impl;

import java.util.Collection;
import java.util.Map;

import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class VariantRuntimeTestFacade {
	
	private VariantRuntime runtime;
	
	public VariantRuntimeTestFacade(VariantRuntime runtime) {
		this.runtime = runtime;
	}
	/**
	 * 
	 * @param view
	 * @param vector
	 * @return
	 */
	public Map<String,String> resolveState(State state, Collection<Experience> vector) {
		return runtime.resolveState(state, vector);
	}
	
	/**
	 * 
	 * @param V
	 * @return
	 */
	public boolean isResolvable(Collection<Experience> v) {
		return runtime.isResolvable(v);
	}

	/**
	 * 
	 * @param V
	 * @param W
	 * @return
	 */
	public Collection<Experience> minUnresolvableSubvector(Collection<Experience> v, Collection<Experience> w) {
		return runtime.minUnresolvableSubvector(v, w);
	}

	/**
	 * 
	 * @param test
	 * @param alreadyTargetedExperiences
	 * @return
	 */
	public boolean isTargetable(Test test, Collection<Experience> alreadyTargetedExperiences) {
		return runtime.isTargetable(test, alreadyTargetedExperiences);
	}

}
