package com.variant.server.impl;

import java.util.Random;

import com.variant.core.impl.ServerError;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.server.api.ServerException;
import com.variant.server.api.lifecycle.PostResultFactory;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;

class VariationTargetingDefaultHook implements LifecycleHook<VariationTargetingLifecycleEvent> {
	
	private static Random rand = new Random(System.currentTimeMillis());

	/**
	 * Package visibility
	 */
	VariationTargetingDefaultHook() {}
	
	@Override
	public Class<VariationTargetingLifecycleEvent> getLifecycleEventClass() {
		return VariationTargetingLifecycleEvent.class;
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
	public VariationTargetingLifecycleEvent.PostResult post(VariationTargetingLifecycleEvent event) throws Exception {
		
		Variation var = event.getVariation();
		State state = event.getState();

		double weightSum = 0;
		for (Experience e: var.getExperiences()) {
			if (e.isPhantom(state)) continue;
			if (e.getWeight() == null ) {
				// It's not a syntax error not to supply the weight, but if we're
				// here it means that no targeter hook fired, and that's a runtime error.
				throw new ServerException.Local(ServerError.EXPERIENCE_WEIGHT_MISSING, e.getVariation().getName(), e.getName());
			}
			weightSum += e.getWeight().doubleValue();
		}

		if (weightSum == 0) {
			throw new ServerException.Internal(
					String.format("No defined states in test [%s] on state [%s]", var.getName(), state.getName()));
		}
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		for (Experience e: var.getExperiences()) {
			if (e.isPhantom(state)) continue;
			weightSum += e.getWeight().doubleValue();
			if (randVal < weightSum) {
				VariationTargetingLifecycleEvent.PostResult result = PostResultFactory.mkPostResult(event);
				result.setTargetedExperience(e);
				return result;
			}
		}
		
		// Should never happen.						
		throw new ServerException.Internal("Error in default targeter.");
	}

}

