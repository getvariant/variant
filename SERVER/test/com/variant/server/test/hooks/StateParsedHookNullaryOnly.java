package com.variant.server.test.hooks;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.StateParsedLifecycleEvent;

public class StateParsedHookNullaryOnly implements LifecycleHook<StateParsedLifecycleEvent> {
		
	public StateParsedHookNullaryOnly() {}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {return null;}
}