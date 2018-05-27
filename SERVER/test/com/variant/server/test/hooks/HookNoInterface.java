package com.variant.server.test.hooks;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.TestQualificationLifecycleEvent;

/**
 * This class does not implement the required interface.
 */
public class HookNoInterface {

	public static String ATTR_KEY = TestQualificationHookNil.class.getName();;
	
	public void foo(String bar) {}

    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	public LifecycleHook.PostResult post(TestQualificationLifecycleEvent event) {
		return null;
	}
}
