package com.variant.core.schema.parser;

import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.Test;
import com.variant.core.schema.TestParsedLifecycleEvent;

/**
 * 
 * @author Igor
 *
 */
class TestParsedHookImpl implements TestParsedLifecycleEvent {

	private Test test;
	private ParserResponse response;
	
	TestParsedHookImpl(Test test, ParserResponse response) {
		this.test = test;
		this.response = response;
	}
	
	@Override
	public Test getTest() {
		return test;
	}

	@Override
	public ParserResponse getParserResponse() {
		return response;
	}

	@Override
    public void addMessage(String message) {
    	((ParserResponseImpl) response).addMessage(message);
    }

}
