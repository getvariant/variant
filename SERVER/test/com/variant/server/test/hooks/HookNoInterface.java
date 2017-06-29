package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.server.api.hook.TestQualificationLifecycleEvent;

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
