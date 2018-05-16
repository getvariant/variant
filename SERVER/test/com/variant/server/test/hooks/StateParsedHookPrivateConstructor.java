package com.variant.server.test.hooks;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.StateParsedLifecycleEvent;

public class StateParsedHookPrivateConstructor implements LifecycleHook<StateParsedLifecycleEvent> {
		
	// ServerHooker won't be able to instantiate
	private StateParsedHookPrivateConstructor() {}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {return null;}
}