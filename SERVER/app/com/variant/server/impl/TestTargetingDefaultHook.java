package com.variant.server.impl;

import java.util.Random;

import com.variant.core.ServerError;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.schema.State;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.server.api.PostResultFactory;
import com.variant.server.api.ServerException;
import com.variant.server.api.lifecycle.TestTargetingLifecycleEvent;

class TestTargetingDefaultHook implements LifecycleHook<TestTargetingLifecycleEvent> {
	
	private static Random rand = new Random(System.currentTimeMillis());

	/**
	 * Package visibility
	 */
	TestTargetingDefaultHook() {}
	
	@Override
	public Class<TestTargetingLifecycleEvent> getLifecycleEventClass() {
		return TestTargetingLifecycleEvent.class;
	}

	/**
	 * Default test qualifier. Qualify all.
	 * 
	 * @param session
	 * @param test
	 * @param state
	 * 
	 */
	@Override
	public TestTargetingLifecycleEvent.PostResult post(TestTargetingLifecycleEvent event) throws Exception {
		
		Test test = event.getTest();
		State state = event.getState();

		double weightSum = 0;
		for (Experience e: test.getExperiences()) {
			if (e.isPhantomOn(state)) continue;
			if (e.getWeight() == null ) {
				// It's not a syntax error not to supply the weight, but if we're
				// here it means that no targeter hook fired, and that's a runtime error.
				throw new ServerException.Local(ServerError.EXPERIENCE_WEIGHT_MISSING, e.getTest().getName(), e.getName());
			}
			weightSum += e.getWeight().doubleValue();
		}

		if (weightSum == 0) {
			throw new ServerException.Internal(
					String.format("No defined states in test [%s] on state [%s]", test.getName(), state.getName()));
		}
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		for (Experience e: test.getExperiences()) {
			if (e.isPhantomOn(state)) continue;
			weightSum += e.getWeight().doubleValue();
			if (randVal < weightSum) {
				TestTargetingLifecycleEvent.PostResult result = PostResultFactory.mkPostResult(event);
				result.setTargetedExperience(e);
				return result;
			}
		}
		
		// Should never happen.						
		throw new ServerException.Internal("Error in default targeter.");
	}

}

