package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.lce.TestTargetingLifecycleEvent;
import com.variant.core.schema.Hook;
import com.variant.server.api.Session;

/**
 * targeting listener does nothing, except logs test names.
 */
public class TestTargetingHookNil implements UserHook<TestTargetingLifecycleEvent> {

	public static String ATTR_KEY = "current-list";
	
	@Override
	public void init(Config config, Hook hook) {}

	@Override
    public Class<TestTargetingLifecycleEvent> getLifecycleEventClass() {
		return TestTargetingLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(TestTargetingLifecycleEvent event) {
		Session ssn = event.getStateRequest().getSession();
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.setAttribute(ATTR_KEY,  curVal + event.getTest().getName());
		return null;
	}

}
