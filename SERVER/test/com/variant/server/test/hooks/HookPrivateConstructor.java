package com.variant.server.test.hooks;

import java.util.Optional;

import com.typesafe.config.Config;
import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class HookPrivateConstructor implements LifecycleHook<VariationQualificationLifecycleEvent> {
		
	/**
	 * Non nullary private constructor -- won't work.
	 * @param config
	 */
	private HookPrivateConstructor(Config conf) {}
	
	@Override
    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) {
		return Optional.empty();
	}
}