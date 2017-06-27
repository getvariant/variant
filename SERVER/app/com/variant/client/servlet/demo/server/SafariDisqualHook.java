package com.variant.client.servlet.demo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.schema.Hook;
import com.variant.server.api.Session;
import com.variant.server.api.hook.PostResultFactory;
import com.variant.server.api.hook.TestQualificationLifecycleEvent;
import com.variant.server.api.hook.TestQualificationLifecycleEventPostResult;

/**
 * User hook to disqualify traffic from Firefox browsers.
 * Users with Firefox browsers will not participate in the experiment.
 */
public class SafariDisqualHook implements UserHook<TestQualificationLifecycleEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(SafariDisqualHook.class);
	
	@Override
	public void init(Config config, Hook hook) throws Exception {
		// No configuration.
	}

	@Override
	public Class<TestQualificationLifecycleEvent> getLifecycleEventClass() {
		return TestQualificationLifecycleEvent.class;
	}

	@Override
	public UserHook.PostResult post(TestQualificationLifecycleEvent event) throws Exception {

		Session ssn = event.getStateRequest().getSession();
		if (ssn.getAttribute("user-agent").matches(".*Safari.*")) {
			LOG.debug("Disqualified Safari session [" + ssn.getId() + "]");
			TestQualificationLifecycleEventPostResult result = PostResultFactory.mkPostResult(event);
			result.setQualified(false);
			return result;
		}
		// Other qual hooks may still disqualify.
		else return null;
	}

}
