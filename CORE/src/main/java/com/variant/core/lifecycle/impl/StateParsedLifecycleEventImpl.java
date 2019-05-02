package com.variant.core.lifecycle.impl;

import com.variant.core.error.ServerError;
import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.lifecycle.StateParsedLifecycleEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

/**
 * 
 * @author Igor
 *
 */
public class StateParsedLifecycleEventImpl implements StateParsedLifecycleEvent {

	private State state;
	private ParserResponse response;
	
	public StateParsedLifecycleEventImpl(State state, ParserResponse response) {
		this.state = state;
		this.response = response;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public LifecycleHook.PostResult newPostResult() {
		
		return new StateParsedLifecycleEvent.PostResult() {
			// Nothing
		};
	}

	@Override
	public State getState() {
		return state;
	}
	
	@Override
	public void addMessage(Severity severity, String message) {
		UserError error = null;
		switch (severity) {
		case INFO: error = ServerError.HOOK_USER_MESSAGE_INFO; break; 
		case WARN: error = ServerError.HOOK_USER_MESSAGE_WARN; break;
		case ERROR: error = ServerError.HOOK_USER_MESSAGE_ERROR; break;
		case FATAL: error = ServerError.HOOK_USER_MESSAGE_ERROR; break;
		}
		
		response.addMessage(error, message);
	}

	/*
	public static class PostResultImpl implements StateParsedLifecycleEvent.PostResult {

		private final StateParsedLifecycleEventImpl event;
		
		public PostResultImpl(StateParsedLifecycleEvent event) {
			this.event = (StateParsedLifecycleEventImpl) event;
		}
		

	}
*/
}
