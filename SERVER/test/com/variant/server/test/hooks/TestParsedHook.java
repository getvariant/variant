package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.Hook;
import com.variant.core.schema.TestParsedLifecycleEvent;
import com.variant.server.api.hook.PostResultFactory;
import com.variant.server.api.hook.StateParsedLifecycleEventPostResult;
import com.variant.server.api.hook.TestParsedLifecycleEventPostResult;

public class TestParsedHook implements UserHook<TestParsedLifecycleEvent> {
	
	public static final String INFO_MESSAGE_FORMAT = "Info-Message-Test %s %s";
	public static final String WARN_MESSAGE_FORMAT = "Warn-Message-Test %s %s";
	public static final String ERROR_MESSAGE_FORMAT = "Error Message Test %s %s";
		
	private Hook hook;
	
	@Override
	public void init(Config config, Hook hook) {
		this.hook = hook;
	}

	@Override
    public Class<TestParsedLifecycleEvent> getLifecycleEventClass() {
		return TestParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(TestParsedLifecycleEvent event) {
		TestParsedLifecycleEventPostResult result = PostResultFactory.mkPostResult(event);
		result.addMessage(Severity.INFO, String.format(INFO_MESSAGE_FORMAT, hook.getName(), event.getTest().getName()));
		result.addMessage(Severity.WARN, String.format(WARN_MESSAGE_FORMAT, hook.getName(), event.getTest().getName()));
		result.addMessage(Severity.ERROR, String.format(ERROR_MESSAGE_FORMAT, hook.getName(), event.getTest().getName()));
		return result;
	}
}