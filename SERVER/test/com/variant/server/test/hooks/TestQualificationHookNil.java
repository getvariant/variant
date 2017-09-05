package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.lce.TestQualificationLifecycleEvent;
import com.variant.core.schema.Hook;
import com.variant.server.api.Session;

/**
 * Do nothing. Tests should be qualified by default.
 */
public class TestQualificationHookNil implements UserHook<TestQualificationLifecycleEvent> {

	public static String ATTR_KEY = TestQualificationHookNil.class.getName();;
	
	@Override
	public void init(Config config, Hook hook) {}

	@Override
    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(TestQualificationLifecycleEvent event) {
		Session ssn = event.getStateRequest().getSession();
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.setAttribute(ATTR_KEY,  curVal + event.getTest().getName());
		return null;
	}
}
