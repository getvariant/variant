package com.variant.server.test.hooks;

import java.util.ArrayList;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.Hook;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.server.api.UserHook;

public class StateParsedHook implements UserHook<StateParsedLifecycleEvent> {
	
	public static final String INFO_MESSAGE_FORMAT = "Info-Message-State %s %s";
	public static final String WARN_MESSAGE_FORMAT = "Warn-Message-State %s %s";
	public static final String ERROR_MESSAGE_FORMAT = "Error Message State %s %s";

	private ArrayList<State> stateList = new ArrayList<State>();
		
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public void post(StateParsedLifecycleEvent event, Hook hook) {
		stateList.add(event.getState());
		event.addMessage(Severity.INFO, String.format(INFO_MESSAGE_FORMAT, hook.getName(), event.getState().getName()));
		event.addMessage(Severity.WARN, String.format(WARN_MESSAGE_FORMAT, hook.getName(), event.getState().getName()));
		event.addMessage(Severity.ERROR, String.format(ERROR_MESSAGE_FORMAT, hook.getName(), event.getState().getName()));
	}
}