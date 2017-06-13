package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.schema.Hook;
import com.variant.server.api.TestQualificationLifecycleEvent;
import com.variant.server.api.UserHook;

public class TestQualificationHookDisqual implements UserHook<TestQualificationLifecycleEvent>{

	private boolean removeFromTargetingTracker = false;

	@Override
	public void init(Config config) {
		removeFromTargetingTracker = config.getBoolean("init.removeFromTargetingTracker");
	}

	@Override
    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	@Override
	public void post(TestQualificationLifecycleEvent event, Hook hook) {		
		event.setQualified(false);
		event.setRemoveFromTargetingTracker(removeFromTargetingTracker);
	}
	
}
