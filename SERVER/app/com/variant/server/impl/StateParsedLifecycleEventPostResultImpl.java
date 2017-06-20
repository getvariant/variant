package com.variant.server.impl;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.StateParsedLifecycleEvent;
import com.variant.core.schema.parser.ParserResponseImpl;
import com.variant.core.schema.parser.StateParsedLifecycleEventImpl;
import com.variant.server.api.hook.StateParsedLifecycleEventPostResult;

public class StateParsedLifecycleEventPostResultImpl implements StateParsedLifecycleEventPostResult {

	StateParsedLifecycleEventImpl event = null;
	
	public StateParsedLifecycleEventPostResultImpl(StateParsedLifecycleEvent event) {
		this.event = (StateParsedLifecycleEventImpl) event;
	}
	
	@Override
	public void addMessage(Severity severity, String message) {
		ParserResponse response = ((StateParsedLifecycleEventImpl) event).getParserResponse();
		((ParserResponseImpl) response).addMessage(severity, message);
	}

}
