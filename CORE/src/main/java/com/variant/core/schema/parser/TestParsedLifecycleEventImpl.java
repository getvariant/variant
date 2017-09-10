package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.UserHook;
import com.variant.core.lce.TestParsedLifecycleEvent;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.Test;

/**
 * 
 * @author Igor
 *
 */
public class TestParsedLifecycleEventImpl implements TestParsedLifecycleEvent {

	private Test test;
	private ParserResponseImpl response;
	
	TestParsedLifecycleEventImpl(Test test, ParserResponseImpl response) {
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
	public ParserResponse getParserResponse() {
    	return response;
    }

	@Override
	public void addMessage(Severity severity, String message) {
		response.addMessage(severity, message);
	}

	@Override
	public UserHook<TestParsedLifecycleEvent> getDefaultHook() {

		return new UserHook<TestParsedLifecycleEvent>() {

			@Override
			public Class<TestParsedLifecycleEvent> getLifecycleEventClass() {
				return TestParsedLifecycleEvent.class;
			}

			@Override
			public UserHook.PostResult post(TestParsedLifecycleEvent event) {
					
				return new TestParsedLifecycleEvent.PostResult() {};
				
			}	
		};
	}

}