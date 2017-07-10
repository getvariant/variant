package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.server.api.hook.PostResultFactory;
import com.variant.server.api.hook.TestQualificationLifecycleEvent;

public class TestQualificationHookDisqual implements UserHook<TestQualificationLifecycleEvent>{

	private boolean removeFromTargetingTracker = false;

	@Override
	public void init(Config config, Hook hook) {
		removeFromTargetingTracker = config.getBoolean("init.removeFromTargetingTracker");
	}

	@Override
    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(TestQualificationLifecycleEvent event) {
		TestQualificationLifecycleEvent.PostResult result = PostResultFactory.mkPostResult(event);
		result.setQualified(false);
		result.setRemoveFromTargetingTracker(removeFromTargetingTracker);
		return result;
	}
	
}
