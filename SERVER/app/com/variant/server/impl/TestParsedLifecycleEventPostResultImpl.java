package com.variant.server.impl;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.TestParsedLifecycleEvent;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.TestParsedLifecycleEventImpl;

public class TestParsedLifecycleEventPostResultImpl implements TestParsedLifecycleEvent.PostResult {

	TestParsedLifecycleEventImpl event = null;
	
	public TestParsedLifecycleEventPostResultImpl(TestParsedLifecycleEvent event) {
		this.event = (TestParsedLifecycleEventImpl) event;
	}
	
	@Override
	public void addMessage(Severity severity, String message) {
		ParserResponse response = ((TestParsedLifecycleEventImpl) event).getParserResponse();
		((ParserResponseImpl) response).addMessage(severity, message);
	}

}
