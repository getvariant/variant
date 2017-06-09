package com.variant.server.test.hooks;

import com.typesafe.config.ConfigValue;
import com.variant.core.schema.Hook;
import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.server.api.UserHook;

public class StateParsedHookPrivateConstructor implements UserHook<StateParsedLifecycleEvent> {
		
	// ServerHooker won't be able to instantiate
	private StateParsedHookPrivateConstructor() {}
	
	@Override
	public void init(ConfigValue init) {}

	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public void post(StateParsedLifecycleEvent event, Hook hook) {}
}