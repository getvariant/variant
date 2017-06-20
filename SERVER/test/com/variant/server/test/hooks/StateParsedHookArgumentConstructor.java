package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.core.schema.StateParsedLifecycleEvent;

public class StateParsedHookArgumentConstructor implements UserHook<StateParsedLifecycleEvent> {
	
	// ServerHooker won't be able to instantiate
	public StateParsedHookArgumentConstructor(String badArgument) {}
	
	@Override
	public void init(Config config, Hook hook) {}

	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {return null;}
}