package com.variant.server.test.hooks;

import com.variant.core.UserHook;
import com.variant.core.lce.StateParsedLifecycleEvent;

public class StateParsedHookNullaryOnly implements UserHook<StateParsedLifecycleEvent> {
		
	public StateParsedHookNullaryOnly() {}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {return null;}
}