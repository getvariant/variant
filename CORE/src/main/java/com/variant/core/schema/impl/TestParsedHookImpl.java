package com.variant.core.schema.impl;

import com.variant.core.hook.TestParsedHook;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserResponse;

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

}
