package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.PostResultFactory;
import com.variant.server.lifecycle.TestQualificationLifecycleEvent;

public class TestQualificationHookDisqual implements LifecycleHook<TestQualificationLifecycleEvent>{

	private boolean removeFromTargetingTracker = false;

	/**
	 * Non nullary constructor
	 * @param config
	 */
	public TestQualificationHookDisqual(Config config) {
		removeFromTargetingTracker = config.getBoolean("removeFromTargetingTracker");
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
