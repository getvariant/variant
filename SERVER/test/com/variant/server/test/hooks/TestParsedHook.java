package com.variant.server.test.hooks;

import com.typesafe.config.ConfigValue;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.Hook;
import com.variant.core.schema.TestParsedLifecycleEvent;
import com.variant.server.api.UserHook;

public class TestParsedHook implements UserHook<TestParsedLifecycleEvent> {
	
	public static final String INFO_MESSAGE_FORMAT = "Info-Message-Test %s %s";
	public static final String WARN_MESSAGE_FORMAT = "Warn-Message-Test %s %s";
	public static final String ERROR_MESSAGE_FORMAT = "Error Message Test %s %s";
		
	@Override
	public void init(ConfigValue init) {}

	@Override
    public Class<TestParsedLifecycleEvent> getLifecycleEventClass() {
		return TestParsedLifecycleEvent.class;
    }
   
	@Override
	public void post(TestParsedLifecycleEvent event, Hook hook) {
		event.addMessage(Severity.INFO, String.format(INFO_MESSAGE_FORMAT, hook.getName(), event.getTest().getName()));
		event.addMessage(Severity.WARN, String.format(WARN_MESSAGE_FORMAT, hook.getName(), event.getTest().getName()));
		event.addMessage(Severity.ERROR, String.format(ERROR_MESSAGE_FORMAT, hook.getName(), event.getTest().getName()));
	}
}