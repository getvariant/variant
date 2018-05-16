package com.variant.server.test.hooks;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.Session;
import com.variant.server.lifecycle.TestTargetingLifecycleEvent;

/**
 * targeting listener does nothing, except logs test names.
 */
public class TestTargetingHookNil implements LifecycleHook<TestTargetingLifecycleEvent> {

	public static String ATTR_KEY = "current-list";
	
	@Override
    public Class<TestTargetingLifecycleEvent> getLifecycleEventClass() {
		return TestTargetingLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(TestTargetingLifecycleEvent event) {
		Session ssn = event.getSession();
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.setAttribute(ATTR_KEY,  curVal + event.getTest().getName());
		return null;
	}

}
