package com.variant.server.test.hooks;

import java.util.Optional;

import com.typesafe.config.Config;
import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class HookPrivateConstructor implements LifecycleHook<VariationQualificationLifecycleEvent> {
		
	/**
	 * Private constructor -- won't work.
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