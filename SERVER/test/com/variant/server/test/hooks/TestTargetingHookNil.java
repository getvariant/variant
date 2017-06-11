package com.variant.server.test.hooks;

import com.typesafe.config.ConfigValue;
import com.variant.core.schema.Hook;
import com.variant.server.api.Session;
import com.variant.server.api.TestTargetingLifecycleEvent;
import com.variant.server.api.UserHook;

/**
 * targeting listener does nothing, except increments the post counter.
 */
public class TestTargetingHookNil implements UserHook<TestTargetingLifecycleEvent> {

	public static String ATTR_KEY = "current-list";
	
	@Override
	public void init(ConfigValue init) {}

	@Override
    public Class<TestTargetingLifecycleEvent> getLifecycleEventClass() {
		return TestTargetingLifecycleEvent.class;
    }
   
	@Override
	public void post(TestTargetingLifecycleEvent event, Hook hook) {
		Session ssn = event.getStateRequest().getSession();
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = "";
		ssn.setAttribute(ATTR_KEY,  curVal + " " + event.getTest().getName());
	}

}
