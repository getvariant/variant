package com.variant.server.impl;

import com.variant.core.lce.TestParsedLifecycleEvent;
import com.variant.core.schema.parser.TestParsedLifecycleEventImpl;

public class TestParsedLifecycleEventPostResultImpl implements TestParsedLifecycleEvent.PostResult {

	TestParsedLifecycleEventImpl event = null;
	
	public TestParsedLifecycleEventPostResultImpl(TestParsedLifecycleEvent event) {
		this.event = (TestParsedLifecycleEventImpl) event;
	}
	
}
