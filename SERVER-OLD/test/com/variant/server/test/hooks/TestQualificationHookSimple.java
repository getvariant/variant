package com.variant.server.test.hooks;

import java.util.Optional;

import com.typesafe.config.Config;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.LifecycleEvent;
import com.variant.server.api.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

/**
 * Do nothing. Tests should be qualified by default.
 */
public class TestQualificationHookSimple implements LifecycleHook<VariationQualificationLifecycleEvent> {

	public static String ATTR_NAME = TestQualificationHookSimple.class.getName();
	
	private final String attrValue;
	
	public TestQualificationHookSimple(Config config) {

		attrValue = config.getString("value");
	}

	@Override
    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }

	@Override
	public Optional<LifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) {
		Session ssn = event.getSession();
		String curVal = ssn.getAttributes().get(ATTR_NAME);
		String newVal = attrValue + '.' + event.getVariation().getName();
		ssn.getAttributes().put(ATTR_NAME,  curVal == null ? newVal : curVal + " " + newVal);
		return Optional.empty();
	}
}
