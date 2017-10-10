package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.UserHook;
import com.variant.core.lce.TestParsedLifecycleEvent;
import com.variant.core.schema.Test;
import com.variant.core.schema.parser.error.CollateralMessage;

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
	public void addMessage(Severity severity, String message) {
		CollateralMessage cm = null;
		switch (severity) {
		case INFO: cm = CollateralMessage.HOOK_USER_MESSAGE_INFO;
		case WARN: cm = CollateralMessage.HOOK_USER_MESSAGE_INFO;
		case ERROR: cm = CollateralMessage.HOOK_USER_MESSAGE_ERROR;
		case FATAL: cm = CollateralMessage.HOOK_USER_MESSAGE_ERROR;
		}
		
		response.addMessage(cm, message);
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
