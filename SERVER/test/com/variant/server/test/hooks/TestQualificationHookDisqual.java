package com.variant.server.test.hooks;

import java.util.Optional;

import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class TestQualificationHookDisqual implements LifecycleHook<VariationQualificationLifecycleEvent>{

	/**
	 * Non nullary constructor
	 * @param config
	 */
	public TestQualificationHookDisqual() {}

	@Override
    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) {
		VariationQualificationLifecycleEvent.PostResult result = event.mkPostResult();
		result.setQualified(false);
		return Optional.of(result);
	}
	
}
