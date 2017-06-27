package com.variant.client.servlet.demo.server;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.server.api.Session;
import com.variant.server.api.hook.TestQualificationLifecycleEvent;

/**
 * User hook to disqualify traffic from Firefox browsers.
 * Users with Firefox browsers will not participate in the experiment.
 */
public class FirefoxDisqualHook implements UserHook<TestQualificationLifecycleEvent> {

	@Override
	public void init(Config config, Hook hook) throws Exception {
		// No configuration.
	}

	@Override
	public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
	}

	@Override
	public com.variant.core.UserHook.PostResult post(
			TestQualificationLifecycleEvent event) throws Exception {
		Session ssn = event.getStateRequest().getSession();
		System.out.println("************************: " + ssn.getAttribute("user-agent"));
		return null;
	}

}
