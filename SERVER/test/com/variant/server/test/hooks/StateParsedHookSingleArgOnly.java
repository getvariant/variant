package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.lce.StateParsedLifecycleEvent;

public class StateParsedHookSingleArgOnly implements UserHook<StateParsedLifecycleEvent> {
		
	/**
	 * Non nullary constructor
	 * @param config
	 */
	public StateParsedHookSingleArgOnly(Config conf) {}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {return null;}
}