package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.variant.core.schema.Hook;
import com.variant.core.schema.Test;
import com.variant.server.api.TestQualificationLifecycleEvent;
import com.variant.server.api.UserHook;

public class TestQualificationHookDisqual implements UserHook<TestQualificationLifecycleEvent>{

	public static String ATTR_KEY = "current-list";
	
	private boolean removeFromTargetingTracker = false;
	
	@Override
	public void init(ConfigValue init) {
		Config config = init.atKey("init");
		removeFromTargetingTracker = config.getBoolean("init.removeFromTargetingTracker");
	}

	@Override
    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	@Override
	public void post(TestQualificationLifecycleEvent event, Hook hook) {
		//event.getSession() != null, "No session passed"
		//event.getTest() != null, "No test passed"
		Test test = event.getTest();
		event.getStateRequest().getSession().setAttribute(ATTR_KEY, test.getName());
		event.setQualified(false);
		event.setRemoveFromTargetingTracker(removeFromTargetingTracker);
	}
	
}
