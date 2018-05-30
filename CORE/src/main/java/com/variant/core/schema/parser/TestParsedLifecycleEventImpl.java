package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.impl.CommonError;
import com.variant.core.impl.ServerError;
import com.variant.core.lifecycle.TestParsedLifecycleEvent;
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
		CommonError error = null;
		switch (severity) {
		case INFO: error = ServerError.HOOK_USER_MESSAGE_INFO; break;
		case WARN: error = ServerError.HOOK_USER_MESSAGE_WARN; break;
		case ERROR: error = ServerError.HOOK_USER_MESSAGE_ERROR; break;
		case FATAL: error = ServerError.HOOK_USER_MESSAGE_ERROR; break;
		}
		
		response.addMessage(error, message);
	}

}
