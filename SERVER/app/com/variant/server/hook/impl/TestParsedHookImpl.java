package com.variant.server.hook.impl;

import com.variant.core.xdm.Test;
import com.variant.server.ParserResponse;
import com.variant.server.ParserMessage.Severity;
import com.variant.server.hook.TestParsedHook;
import com.variant.server.schema.ParserResponseImpl;

/**
 * 
 * @author Igor
 *
 */
class TestParsedHookImpl implements TestParsedHook {

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
    public void addMessage(Severity severity, String message) {
    	((ParserResponseImpl) response).addMessage(severity, message);
    }

}
