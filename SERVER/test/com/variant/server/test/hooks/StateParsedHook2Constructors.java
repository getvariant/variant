package com.variant.server.test.hooks;

import com.typesafe.config.Config;
import com.variant.core.error.UserError.Severity;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.StateParsedLifecycleEvent;

public class StateParsedHook2Constructors implements LifecycleHook<StateParsedLifecycleEvent> {
		
	public static String MSG_NULLARY    = "Nullary Constructor Called";
	public static String MSG_SINGLE_ARG = "Single-arg Constructor Called";
	
	private String msg = null;
	
	public StateParsedHook2Constructors() {
		msg = MSG_NULLARY;
	}

	public StateParsedHook2Constructors(Config conf) {
		msg = MSG_SINGLE_ARG;
	}
	
	@Override
    public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
		return StateParsedLifecycleEvent.class;
    }
   
	@Override
	public PostResult post(StateParsedLifecycleEvent event) {
	
		event.addMessage(Severity.INFO, msg);
		return null;
	}
}