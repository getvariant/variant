package com.variant.core.schema.parser;

import com.variant.core.UserHook;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.Test;
import com.variant.core.schema.TestParsedLifecycleEvent;

/**
 * 
 * @author Igor
 *
 */
public class TestParsedLifecycleEventImpl implements TestParsedLifecycleEvent {

	private Test test;
	private ParserResponse response;
	
	TestParsedLifecycleEventImpl(Test test, ParserResponse response) {
		this.test = test;
		this.response = response;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public Test getTest() {
		return test;
	}

	@Override
	public UserHook<TestParsedLifecycleEvent> getDefaultHook() {
		return null;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public ParserResponse getParserResponse() {
    	return response;
    }

}
