package com.variant.core.impl;

import java.util.Collection;
import java.util.Map;

import com.variant.core.Variant;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;

public class VariantRuntimeTestFacade {
	
	private VariantRuntime runtime;
	
	public VariantRuntimeTestFacade(Variant api) {
		this.runtime = ((VariantCoreImpl)api).getRuntime();
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
	 * @param vector
	 * @return
	 */
	public Collection<Experience> minUnresolvableSubvector(Collection<Experience> vector) {
		return runtime.minUnresolvableSubvector(vector);
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
