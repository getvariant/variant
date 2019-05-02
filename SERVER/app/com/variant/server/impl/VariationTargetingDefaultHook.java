package com.variant.server.impl;

import java.util.Optional;
import java.util.Random;

import com.variant.core.error.ServerError;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;
import com.variant.server.boot.ServerExceptionInternal;
import com.variant.server.boot.ServerExceptionLocal;

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
	public Optional<VariationTargetingLifecycleEvent.PostResult> post(VariationTargetingLifecycleEvent event) throws Exception {
		
		Variation var = event.getVariation();
		State state = event.getState();

		double weightSum = 0;
		for (Experience e: var.getExperiences()) {
			if (e.isPhantom(state)) continue;
			if (!e.getWeight().isPresent()) {
				// It's not a syntax error not to supply the weight, but if we're
				// here it means that no targeter hook fired, and that's a runtime error.
				throw new ServerExceptionLocal(ServerError.EXPERIENCE_WEIGHT_MISSING, e.getVariation().getName(), e.getName());
			}
			weightSum += e.getWeight().get().doubleValue();
		}

		if (weightSum == 0) {
			throw new ServerExceptionInternal(
					String.format("No defined states in test [%s] on state [%s]", var.getName(), state.getName()));
		}
		
		double randVal = rand.nextDouble() * weightSum;
		weightSum = 0;
		for (Experience e: var.getExperiences()) {
			if (e.isPhantom(state)) continue;
			weightSum += e.getWeight().get().doubleValue();
			if (randVal < weightSum) {
				VariationTargetingLifecycleEvent.PostResult result = event.newPostResult();
				result.setTargetedExperience(e);
				return Optional.of(result);
			}
		}
		
		// Should never happen.						
		throw new ServerExceptionInternal("Error in default targeter.");
	}

}

