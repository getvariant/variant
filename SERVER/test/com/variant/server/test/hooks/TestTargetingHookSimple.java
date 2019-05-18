package com.variant.server.test.hooks;

import java.util.Optional;

import com.typesafe.config.Config;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationTargetingLifecycleEvent;

/**
 * Targeting listener appends the string value passed in the init to a known session attribute.
 * Tests can figure out the order in which hooks were posted.
 */
public class TestTargetingHookSimple implements LifecycleHook<VariationTargetingLifecycleEvent> {

	public static final String ATTR_NAME = TestTargetingHookSimple.class.getName();

	private final String attrValue;
	
	public TestTargetingHookSimple(Config config) {

		attrValue = config.getString("value");
	}

	@Override
    public Class<VariationTargetingLifecycleEvent> getLifecycleEventClass() {
		return VariationTargetingLifecycleEvent.class;
    }

	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationTargetingLifecycleEvent event) {
		Session ssn = event.getSession();
		String curVal = ssn.getAttributes().get(ATTR_NAME);
		String newVal = attrValue + '.' + event.getVariation().getName() + "." + event.getState().getName();
		ssn.getAttributes().put(ATTR_NAME,  curVal == null ? newVal : curVal + " " + newVal);
		return Optional.empty();
	}

}
