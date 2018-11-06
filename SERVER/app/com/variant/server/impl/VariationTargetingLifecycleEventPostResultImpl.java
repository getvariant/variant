package com.variant.server.impl;

import static com.variant.core.impl.ServerError.HOOK_TARGETING_BAD_EXPERIENCE;

import com.variant.core.schema.State;
import com.variant.core.schema.Variation;
import com.variant.core.schema.Variation.Experience;
import com.variant.server.api.ServerException;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;
import com.variant.server.boot.ServerExceptionLocal;

public class VariationTargetingLifecycleEventPostResultImpl implements VariationTargetingLifecycleEvent.PostResult {

	VariationTargetingLifecycleEventImpl event;
	private Experience experience = null;
	
	/**
	 * 
	 * @param event
	 */
	public VariationTargetingLifecycleEventPostResultImpl(VariationTargetingLifecycleEvent event) {
		this.event = (VariationTargetingLifecycleEventImpl) event;
	}
	
	@Override
	public void setTargetedExperience(Experience experience) {

		Variation var = event.getVariation();
		State state = event.getState();
		
		for (Experience te: var.getExperiences()) {
			if (experience.equals(te)) {
				if (experience.isPhantom(state)) {
					StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
					throw new ServerExceptionLocal(
							HOOK_TARGETING_BAD_EXPERIENCE, 
							caller.getClassName(), var.getName(), experience.toString(), var.getName());
				}
				this.experience = experience;
				return;
			}
		}
		// If we're here, the experience is not from the test we're listening for.
		// Figure out the caller class and throw an exception.
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		throw new ServerExceptionLocal(
				HOOK_TARGETING_BAD_EXPERIENCE, 
				caller.getClassName(), var.getName(), experience.toString());
	}

	public Experience getTargetedExperience() {
		return experience;
	}

}
