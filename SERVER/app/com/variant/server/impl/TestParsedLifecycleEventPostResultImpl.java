package com.variant.server.impl;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.TestParsedLifecycleEvent;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.StateParsedLifecycleEventImpl;
import com.variant.server.api.hook.TestParsedLifecycleEventPostResult;

public class TestParsedLifecycleEventPostResultImpl implements TestParsedLifecycleEventPostResult {

	StateParsedLifecycleEventImpl event = null;
	
	public TestParsedLifecycleEventPostResultImpl(TestParsedLifecycleEvent event) {
		this.event = (StateParsedLifecycleEventImpl) event;
	}
	
	@Override
	public void addMessage(Severity severity, String message) {
		ParserResponse response = ((StateParsedLifecycleEventImpl) event).getParserResponse();
		((ParserResponseImpl) response).addMessage(severity, message);
	}

}
