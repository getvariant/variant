package com.variant.server.test.hooks;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.Session;
import com.variant.server.lifecycle.TestQualificationLifecycleEvent;

/**
 * Do nothing. Tests should be qualified by default.
 */
public class TestQualificationHookNil implements LifecycleHook<TestQualificationLifecycleEvent> {

	public static String ATTR_KEY = TestQualificationHookNil.class.getName();
	
	@Override
    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	/**
	 * Append triggering test name to a session attribute.
	 */
	@Override
	public PostResult post(TestQualificationLifecycleEvent event) {
		Session ssn = event.getSession();
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.setAttribute(ATTR_KEY,  curVal + event.getTest().getName());
		return null;
	}
}
