package com.variant.server.impl;

import com.variant.core.lifecycle.StateParsedLifecycleEvent;
import com.variant.core.schema.parser.StateParsedLifecycleEventImpl;

public class StateParsedLifecycleEventPostResultImpl implements StateParsedLifecycleEvent.PostResult {

	StateParsedLifecycleEventImpl event = null;
	
	public StateParsedLifecycleEventPostResultImpl(StateParsedLifecycleEvent event) {
		this.event = (StateParsedLifecycleEventImpl) event;
	}
	

}
