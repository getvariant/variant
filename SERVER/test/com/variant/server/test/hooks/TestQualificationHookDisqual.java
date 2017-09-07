package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.server.api.PostResultFactory;
import com.variant.server.lce.TestQualificationLifecycleEvent;

public class TestQualificationHookDisqual implements UserHook<TestQualificationLifecycleEvent>{

	private boolean removeFromTargetingTracker = false;

	/**
	 * Non nullary constructor
	 * @param config
	 */
	public TestQualificationHookDisqual(Config config) {
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
