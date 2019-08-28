package com.variant.server.test.hooks;
import java.util.Optional;

import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;

public class HookNullaryConstructor implements LifecycleHook<VariationTargetingLifecycleEvent> {
		
	public HookNullaryConstructor() {}
	
	@Override
    public Class<VariationTargetingLifecycleEvent> getLifecycleEventClass() {
		return VariationTargetingLifecycleEvent.class;
    }
   
	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationTargetingLifecycleEvent event) {
		return Optional.empty();
	}

}
