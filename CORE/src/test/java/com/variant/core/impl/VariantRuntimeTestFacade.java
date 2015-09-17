package com.variant.core.impl;

import java.util.Collection;
import java.util.Map;

import com.variant.core.impl.VariantRuntime;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.State;

public class VariantRuntimeTestFacade {
	
	/**
	 * 
	 * @param view
	 * @param vector
	 * @return
	 */
	public static Map<String,String> resolveState(State state, Collection<Experience> vector) {
		return VariantRuntime.resolveState(state, vector);
	}
	
	/**
	 * 
	 * @param vector
	 * @return
	 */
	public static Collection<Experience> minUnresolvableSubvector(Collection<Experience> vector) {
		return VariantRuntime.minUnresolvableSubvector(vector);
	}

	/**
	 * 
	 * @param test
	 * @param alreadyTargetedExperiences
	 * @return
	 */
	public static boolean isTargetable(Test test, Collection<Experience> alreadyTargetedExperiences) {
		return VariantRuntime.isTargetable(test, alreadyTargetedExperiences);
	}

}
