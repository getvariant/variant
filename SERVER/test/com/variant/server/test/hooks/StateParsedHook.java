package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.UserHook;
import com.variant.core.UserError.Severity;
import com.variant.core.schema.Hook;
import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.server.api.hook.PostResultFactory;
import com.variant.server.api.hook.StateParsedLifecycleEventPostResult;

public class StateParsedHook implements UserHook<StateParsedLifecycleEvent> {
	
	public static final String INFO_MESSAGE_FORMAT = "Info-Message-State %s %s";
	public static final String WARN_MESSAGE_FORMAT = "Warn-Message-State %s %s";
	public static final String ERROR_MESSAGE_FORMAT = "Error Message State %s %s";
		
	private Hook hook;
	@Override
	public void init(Config config, Hook hook) {
		this.hook = hook;
	}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {
		StateParsedLifecycleEventPostResult result = PostResultFactory.mkPostResult(event);
		result.addMessage(Severity.INFO, String.format(INFO_MESSAGE_FORMAT, hook.getName(), event.getState().getName()));
		result.addMessage(Severity.WARN, String.format(WARN_MESSAGE_FORMAT, hook.getName(), event.getState().getName()));
		result.addMessage(Severity.ERROR, String.format(ERROR_MESSAGE_FORMAT, hook.getName(), event.getState().getName()));
		return result;
	}
}