package com.variant.server.test.hooks;

import com.variant.core.UserHook;
import com.variant.core.lce.StateParsedLifecycleEvent;

public class StateParsedHookArgumentConstructor implements UserHook<StateParsedLifecycleEvent> {
	
	// ServerHooker won't be able to instantiate
	public StateParsedHookArgumentConstructor(String badArgument) {}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {return null;}
}