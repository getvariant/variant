package com.variant.server.test.hooks;

import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.Session;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

/**
 * Do nothing. Tests should be qualified by default.
 */
public class TestQualificationHookNil implements LifecycleHook<VariationQualificationLifecycleEvent> {

	public static String ATTR_KEY = TestQualificationHookNil.class.getName();
	
	@Override
    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	/**
	 * Append triggering test name to a session attribute.
	 */
	@Override
	public PostResult post(VariationQualificationLifecycleEvent event) {
		Session ssn = event.getSession();
		String curVal = ssn.getAttributes().get(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.getAttributes().put(ATTR_KEY,  curVal + event.getVariation().getName());
		return null;
	}
}
