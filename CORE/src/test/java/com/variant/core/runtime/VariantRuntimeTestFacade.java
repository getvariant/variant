package com.variant.core.runtime;

import java.util.Set;

import com.variant.core.schema.Test;

public class VariantRuntimeTestFacade {
	
	public boolean isResolvable(Set<Test> tests) {
		return VariantRuntime.isResolvable(tests);
	}

}
