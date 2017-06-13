package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.schema.Hook;
import com.variant.server.api.Session;
import com.variant.server.api.TestQualificationLifecycleEvent;
import com.variant.server.api.UserHook;

/**
 * Do nothing. Tests should be qualified by default.
 */
public class TestQualificationHookNil implements UserHook<TestQualificationLifecycleEvent> {

	public static String ATTR_KEY = "current-list";
	
	@Override
	public void init(Config config) {}

	@Override
    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	@Override
	public void post(TestQualificationLifecycleEvent event, Hook hook) {
		Session ssn = event.getStateRequest().getSession();
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.setAttribute(ATTR_KEY,  curVal + event.getTest().getName());
	}
}
