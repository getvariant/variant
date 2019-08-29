package com.variant.server.test.hooks;

import java.util.Optional;

import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

/**
 * This class does not implement the required interface.
 */
public class HookNoInterface {

	public static String ATTR_KEY = TestQualificationHookSimple.class.getName();;
	
	public void foo(String bar) {}

    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	public Optional<LifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) {
		return null;
	}
}
