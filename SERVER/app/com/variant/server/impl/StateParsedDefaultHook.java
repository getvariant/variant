package com.variant.server.impl;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.server.api.PostResultFactory;

class StateParsedDefaultHook implements UserHook<StateParsedLifecycleEvent> {
	
	/**
	 * Package visibility
	 */
	StateParsedDefaultHook() {}
	
	@Override
	public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
	}

	@Override
	public void init(Config config, Hook hook) throws Exception {}

	/**
	 * The default is do nothing.
	 */
	@Override
	public UserHook.PostResult post(StateParsedLifecycleEvent event) throws Exception {		
		return PostResultFactory.mkPostResult(event);				
	}

}
