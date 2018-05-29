package com.variant.core.schema.parser;

import com.variant.core.impl.CommonError;
import com.variant.core.impl.ServerError;
import com.variant.core.impl.UserError.Severity;
import com.variant.core.lifecycle.StateParsedLifecycleEvent;
import com.variant.core.lifecycle.LifecycleHook;
import com.variant.core.schema.State;

/**
 * 
 * @author Igor
 *
 */
public class StateParsedLifecycleEventImpl implements StateParsedLifecycleEvent {

	private State state;
	private ParserResponse response;
	
	StateParsedLifecycleEventImpl(State state, ParserResponse response) {
		this.state = state;
		this.response = response;
	}

	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public State getState() {
		return state;
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

	@Override
	public LifecycleHook<StateParsedLifecycleEvent> getDefaultHook() {
		
		return new LifecycleHook<StateParsedLifecycleEvent>() {

			@Override
			public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
				return StateParsedLifecycleEvent.class;
			}

			@Override
			public LifecycleHook.PostResult post(StateParsedLifecycleEvent event) {
				return new StateParsedLifecycleEvent.PostResult() {};
				
			}	
		};
	}

}
