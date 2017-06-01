package com.variant.server.test.hooks;

import java.util.ArrayList;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.server.api.UserHook;

public class StateParsedHook implements UserHook<StateParsedLifecycleEvent> {
	
	public static final String INFO_MESSAGE = "Info-Message-State ";
	public static final String WARN_MESSAGE = "Warn-Message-State ";
	public static final String ERROR_MESSAGE = "Error Message State ";

	private ArrayList<State> stateList = new ArrayList<State>();
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public void post(StateParsedLifecycleEvent event) {
		stateList.add(event.getState());
		event.addMessage(Severity.INFO, INFO_MESSAGE + event.getState().getName());
		event.addMessage(Severity.WARN, WARN_MESSAGE + event.getState().getName());
		event.addMessage(Severity.ERROR, ERROR_MESSAGE + event.getState().getName());
	}
}