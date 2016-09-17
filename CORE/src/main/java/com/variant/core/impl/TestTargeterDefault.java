package com.variant.core.impl;

import static com.variant.core.xdm.impl.MessageTemplate.RUN_WEIGHT_MISSING;

import java.util.Random;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantInternalException;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.xdm.State;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;

class TestTargeterDefault {

	private static Random rand = new Random(System.currentTimeMillis());
	
	/**
	 * Default random targeter. Ignore experiences undefined on this state.
	 * There should be no state where there are no defined experiences - parse error.
	 * 
	 * @param session
	 * @param test
	 * @param state
	 * 
	 * @return
	 */
	Experience target(VariantCoreSession session, Test test, State state) {

		double weightSum = 0;
		for (Experience e: test.getExperiences()) {
			if (!e.isDefinedOn(state)) continue;
			if (e.getWeight() == null ) {
				// It's not a syntax error not to supply the weight, but if we're
				// here it means that no targeter hook fired, and that's a runtime error.
				throw new VariantRuntimeUserErrorException(RUN_WEIGHT_MISSING, e.getTest().getName(), e.getName());
			}
			weightSum += e.getWeight().doubleValue();
		}

		if (weightSum == 0) {
			throw new VariantInternalException(
					String.format("No defined states in test [%s] on state [%s]", test.getName(), state.getName()));
		}
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		Experience lastExperience = null;
		for (Experience e: test.getExperiences()) {
			if (!e.isDefinedOn(state)) continue;
			lastExperience = e;
			weightSum += e.getWeight().doubleValue();
			if (randVal < weightSum) return e;
		}
		// Should never happen.
		return lastExperience;
	}

}
