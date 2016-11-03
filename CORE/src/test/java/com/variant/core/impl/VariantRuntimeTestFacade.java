package com.variant.core.impl;
/* CLEANUP runtime moved to server.
import java.util.Collection;

import com.variant.core.util.Tuples;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;
import com.variant.core.xdm.impl.StateVariantImpl;
import com.variant.server.runtime.VariantRuntime;

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
	 *
	public Tuples.Pair<Boolean,StateVariantImpl> resolveState(State state, Collection<Experience> vector) {
		return runtime.resolveState(state, vector);
	}
	
	/**
	 * 
	 * @param V
	 * @return
	 *
	public boolean isResolvable(Collection<Experience> v) {
		return runtime.isResolvable(v);
	}

	/**
	 * 
	 * @param V
	 * @param W
	 * @return
	 *
	public Collection<Experience> minUnresolvableSubvector(Collection<Experience> v, Collection<Experience> w) {
		return runtime.minUnresolvableSubvector(v, w);
	}

	/**
	 * 
	 * @param test
	 * @param alreadyTargetedExperiences
	 * @return
	 *
	public boolean isTargetable(Test test, State state, Collection<Experience> alreadyTargetedExperiences) {
		return runtime.isTargetable(test, state, alreadyTargetedExperiences);
	}

}
*/