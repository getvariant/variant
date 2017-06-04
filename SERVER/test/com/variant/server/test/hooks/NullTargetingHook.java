package com.variant.server.test.hooks;

import com.variant.core.schema.Hook;
import com.variant.server.api.TestTargetingLifecycleEvent;
import com.variant.server.api.UserHook;

/**
 * targeting listener does nothing, except increments the post counter.
 */
public class NullTargetingHook implements UserHook<TestTargetingLifecycleEvent> {

	public static int postCount = 0;

	@Override
    public Class<TestTargetingLifecycleEvent> getLifecycleEventClass() {
		return TestTargetingLifecycleEvent.class;
    }
   
	@Override
	public void post(TestTargetingLifecycleEvent event, Hook hook) {
		   postCount += 1;
	}

}
