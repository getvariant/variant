package com.variant.core.schema.impl;

import com.variant.core.flashpoint.TestParsedFlashpoint;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.ParserResponse;

/**
 * 
 * @author Igor
 *
 */
class TestParsedFlashpointImpl implements TestParsedFlashpoint {

	private Test test;
	private ParserResponse response;
	
	TestParsedFlashpointImpl(Test test, ParserResponse response) {
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
