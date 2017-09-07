package com.variant.server.test.hooks;

import com.variant.core.UserHook;
import com.variant.server.lce.TestQualificationLifecycleEvent;

/**
 * This class does not implement the required interface.
 */
public class HookNoInterface {

	public static String ATTR_KEY = TestQualificationHookNil.class.getName();;
	
	public void foo(String bar) {}

    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	public UserHook.PostResult post(TestQualificationLifecycleEvent event) {
		return null;
	}
}
