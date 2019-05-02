package com.variant.server.test.hooks;

import java.util.Optional;

import com.typesafe.config.Config;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.server.api.lifecycle.VariationQualificationLifecycleEvent;

public class Hook2Constructors implements LifecycleHook<VariationQualificationLifecycleEvent> {
		
	public static String MSG_NULLARY    = "Nullary Constructor Called";
	public static String MSG_SINGLE_ARG = "Single-arg Constructor Called";
	
	private String msg = null;
	
	public Hook2Constructors() {
		msg = MSG_NULLARY;
	}

	public Hook2Constructors(Config conf) {
		msg = MSG_SINGLE_ARG;
	}
	
	@Override
    public Class<VariationQualificationLifecycleEvent> getLifecycleEventClass() {
		return VariationQualificationLifecycleEvent.class;
    }
   
	@Override
	public Optional<VariationQualificationLifecycleEvent.PostResult> post(VariationQualificationLifecycleEvent event) {
	
		event.getSession().getAttributes().put("HookWith2Constructors", msg);
		return Optional.empty();
	}
}