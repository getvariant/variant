package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.lce.TestQualificationLifecycleEvent;
import com.variant.core.schema.Hook;

/**
 * This class does not implement the required interface.
 */
public class HookNoInterface {

	public static String ATTR_KEY = TestQualificationHookNil.class.getName();;
	
	public void init(Config config, Hook hook) {}

    public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
    }
   
	public UserHook.PostResult post(TestQualificationLifecycleEvent event) {
		return null;
	}
}
