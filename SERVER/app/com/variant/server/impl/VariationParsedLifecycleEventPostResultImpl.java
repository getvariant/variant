package com.variant.server.impl;

import com.variant.core.lifecycle.VariationParsedLifecycleEvent;
import com.variant.core.schema.parser.TestParsedLifecycleEventImpl;

public class VariationParsedLifecycleEventPostResultImpl implements VariationParsedLifecycleEvent.PostResult {

	TestParsedLifecycleEventImpl event = null;
	
	public VariationParsedLifecycleEventPostResultImpl(VariationParsedLifecycleEvent event) {
		this.event = (TestParsedLifecycleEventImpl) event;
	}
	
}
