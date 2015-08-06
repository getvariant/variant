package com.variant.core.schema.impl;

import java.util.Random;

import com.variant.core.VariantSession;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.core.schema.Test.Targeter;

public class TestTargeterDefault implements Targeter {

	private static Random rand = new Random(System.currentTimeMillis());
	
	@Override
	public Experience target(Test test, VariantSession session) {

		double weightSum = 0;
		for (Experience e: test.getExperiences()) weightSum += e.getWeight();
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		Experience lastExperience = null;
		for (Experience e: test.getExperiences()) {
			lastExperience = e;
			weightSum += e.getWeight();
			if (randVal < weightSum) return e;
		}
		// Should never happen.
		return lastExperience;
	}

}
