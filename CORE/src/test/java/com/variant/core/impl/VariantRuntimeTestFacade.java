package com.variant.core.impl;

import java.util.Collection;

import com.variant.core.impl.VariantRuntime;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.View;

public class VariantRuntimeTestFacade {
	
	/**
	 * 
	 * @param view
	 * @param vector
	 * @return
	 */
	public static String resolveViewPath(View view, Collection<Experience> vector) {
		return VariantRuntime.resolveViewPath(view, vector);
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
