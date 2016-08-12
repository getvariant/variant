package com.variant.core.xdm.impl;

import com.variant.core.hook.TestParsedHook;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.ParserMessage.Severity;
import com.variant.core.xdm.Test;

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
