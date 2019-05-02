package com.variant.server.test.hooks;

import java.util.Optional;

import com.typesafe.config.Config;
import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class TestQualificationHookDisqual implements LifecycleHook<VariationQualificationLifecycleEvent>{

	private boolean removeFromTargetingTracker = false;

	/**
	 * Non nullary constructor
	 * @param config
	 */
	public TestQualificationHookDisqual(Config config) {
		removeFromTargetingTracker = config.getBoolean("removeFromTargetingTracker");
	}

	@Override
    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) {
		VariationQualificationLifecycleEvent.PostResult result = event.newPostResult();
		result.setQualified(false);
		result.setRemoveFromTargetingTracker(removeFromTargetingTracker);
		return Optional.of(result);
	}
	
}
