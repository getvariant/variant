package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.RuntimeError;
import com.variant.core.UserHook;
import com.variant.core.lce.TestParsedLifecycleEvent;
import com.variant.core.schema.Test;

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
		RuntimeError error = null;
		switch (severity) {
		case INFO: error = RuntimeError.HOOK_USER_MESSAGE_INFO; break;
		case WARN: error = RuntimeError.HOOK_USER_MESSAGE_WARN; break;
		case ERROR: error = RuntimeError.HOOK_USER_MESSAGE_ERROR; break;
		case FATAL: error = RuntimeError.HOOK_USER_MESSAGE_ERROR; break;
		}
		
		response.addMessage(error, message);
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
