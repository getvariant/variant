package com.variant.server.test.hooks;

import java.util.Random;

import com.typesafe.config.Config;
import com.variant.core.schema.Hook;
import com.variant.core.schema.Test;
import com.variant.core.schema.Test.Experience;
import com.variant.server.api.Session;
import com.variant.server.api.TestTargetingLifecycleEvent;
import com.variant.server.api.UserHook;

/**
 * targeting listener does nothing, except logs test names.
 */
public class TestTargetingHookAB implements UserHook<TestTargetingLifecycleEvent> {

	public static String ATTR_KEY = TestTargetingHookAB.class.getName();

	private static Random rand = new Random(System.currentTimeMillis());

	@Override
	public void init(Config config) {}

	@Override
    public Class<TestTargetingLifecycleEvent> getLifecycleEventClass() {
		return TestTargetingLifecycleEvent.class;
    }
   
	@Override
	public void post(TestTargetingLifecycleEvent event, Hook hook) {

		Session ssn = event.getStateRequest().getSession();
		Test test = event.getTest();
		
		String curVal = ssn.getAttribute(ATTR_KEY);
		if (curVal == null) curVal = ""; else curVal += " ";
		ssn.setAttribute(ATTR_KEY,  curVal + test.getName());
		
		Experience result = rand.nextBoolean() ? test.getExperience("A") : test.getExperience("B");
		event.setTargetedExperience(result);
	}
}
