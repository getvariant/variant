package com.variant.server.test.hooks;

import java.util.Optional;

import com.variant.core.lifecycle.LifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;

/**
 * targeting listener does nothing, except logs test names.
 */
public class TestTargetingHookNil implements LifecycleHook<VariationTargetingLifecycleEvent> {

	public static String ATTR_KEY = "current-list";
	
	@Override
    public Class<VariationTargetingLifecycleEvent> getLifecycleEventClass() {
		return VariationTargetingLifecycleEvent.class;
    }
   
	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationTargetingLifecycleEvent event) {
		Session ssn = event.getSession();
		String curVal = ssn.getAttributes().get(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.getAttributes().put(ATTR_KEY,  curVal + event.getVariation().getName());
		return Optional.empty();
	}

}
