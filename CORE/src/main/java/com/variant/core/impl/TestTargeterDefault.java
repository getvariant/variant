package com.variant.core.impl;

import static com.variant.core.xdm.impl.MessageTemplate.RUN_WEIGHT_MISSING;

import java.util.Random;

import com.variant.core.VariantCoreSession;
import com.variant.core.exception.VariantRuntimeUserErrorException;
import com.variant.core.xdm.Test;
import com.variant.core.xdm.Test.Experience;

class TestTargeterDefault {

	private static Random rand = new Random(System.currentTimeMillis());
	
	/**
	 * Default random targeter.
	 * @param test
	 * @param session
	 * @return
	 */
	Experience target(Test test, VariantCoreSession session) {

		double weightSum = 0;
		for (Experience e: test.getExperiences()) {
			if (e.getWeight() == null ) {
				// It's not a syntax error not to supply the weight, but if we're
				// here it means that no targeter hook fired, and that's a runtime error.
				throw new VariantRuntimeUserErrorException(RUN_WEIGHT_MISSING, e.getTest().getName(), e.getName());
			}
			weightSum += e.getWeight().doubleValue();
		}
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		Experience lastExperience = null;
		for (Experience e: test.getExperiences()) {
			lastExperience = e;
			weightSum += e.getWeight().doubleValue();
			if (randVal < weightSum) return e;
		}
		// Should never happen.
		return lastExperience;
	}

}
