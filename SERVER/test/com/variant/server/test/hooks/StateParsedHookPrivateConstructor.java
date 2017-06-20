package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.core.schema.StateParsedLifecycleEvent;

public class StateParsedHookPrivateConstructor implements UserHook<StateParsedLifecycleEvent> {
		
	// ServerHooker won't be able to instantiate
	private StateParsedHookPrivateConstructor() {}
	
	@Override
	public void init(Config config, Hook hook) {}

	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public void post(StateParsedLifecycleEvent event) {}
}