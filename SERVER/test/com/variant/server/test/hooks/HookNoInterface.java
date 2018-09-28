package com.variant.server.test.hooks;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

/**
 * This class does not implement the required interface.
 */
public class HookNoInterface {

	public static String ATTR_KEY = TestQualificationHookNil.class.getName();;
	
	public void foo(String bar) {}

    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	public LifecycleHook.PostResult post(VariationQualificationLifecycleEvent event) {
		return null;
	}
}
