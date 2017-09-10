package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserError.Severity;
import com.variant.core.UserHook;
import com.variant.core.lce.TestParsedLifecycleEvent;

public class TestParsedHook implements UserHook<TestParsedLifecycleEvent> {
	
	public static final String INFO_MESSAGE_FORMAT = "Info-Message-Test %s %s";
	public static final String WARN_MESSAGE_FORMAT = "Warn-Message-Test %s %s";
	public static final String ERROR_MESSAGE_FORMAT = "Error Message Test %s %s";
		
	private String hookName = null;
	
	/**
	 * Non nullary constructor
	 * @param config
	 */
	public TestParsedHook(Config config) {
		if (config != null) {
			hookName = config.getString("init.hookName");
		}
	}

	@Override
    public Class<TestParsedLifecycleEvent> getLifecycleEventClass() {
		return TestParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(TestParsedLifecycleEvent event) {
		event.addMessage(Severity.INFO, String.format(INFO_MESSAGE_FORMAT, hookName, event.getTest().getName()));
		event.addMessage(Severity.WARN, String.format(WARN_MESSAGE_FORMAT, hookName, event.getTest().getName()));
		event.addMessage(Severity.ERROR, String.format(ERROR_MESSAGE_FORMAT, hookName, event.getTest().getName()));
		return null;
	}
}