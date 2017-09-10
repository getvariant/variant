package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserError.Severity;
import com.variant.core.UserHook;
import com.variant.core.lce.StateParsedLifecycleEvent;
import com.variant.server.api.PostResultFactory;

public class StateParsedHook implements UserHook<StateParsedLifecycleEvent> {
	
	public static final String INFO_MESSAGE_FORMAT = "Info-Message-State %s %s";
	public static final String WARN_MESSAGE_FORMAT = "Warn-Message-State %s %s";
	public static final String ERROR_MESSAGE_FORMAT = "Error Message State %s %s";
		
	private String hookName = null;
	private boolean clipChain = false;
	
	/**
	 * Non nullary constructor
	 * @param config
	 */
	public StateParsedHook(Config config) {
		if (config != null) {
			hookName = config.getString("init.hookName");
			clipChain = config.getBoolean("init.clipChain");
		}
	}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {
		event.addMessage(Severity.INFO, String.format(INFO_MESSAGE_FORMAT, hookName, event.getState().getName()));
		event.addMessage(Severity.WARN, String.format(WARN_MESSAGE_FORMAT, hookName, event.getState().getName()));
		event.addMessage(Severity.ERROR, String.format(ERROR_MESSAGE_FORMAT, hookName, event.getState().getName()));
		return clipChain ? PostResultFactory.mkPostResult(event) : null;
	}
}