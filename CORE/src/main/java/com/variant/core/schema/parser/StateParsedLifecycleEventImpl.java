package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.UserHook;
import com.variant.core.lce.StateParsedLifecycleEvent;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.error.CollateralMessage;

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
	public UserHook<StateParsedLifecycleEvent> getDefaultHook() {
		
		return new UserHook<StateParsedLifecycleEvent>() {

			@Override
			public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
				return StateParsedLifecycleEvent.class;
			}

			@Override
			public UserHook.PostResult post(StateParsedLifecycleEvent event) {
				return new StateParsedLifecycleEvent.PostResult() {};
				
			}	
		};
	}

}
