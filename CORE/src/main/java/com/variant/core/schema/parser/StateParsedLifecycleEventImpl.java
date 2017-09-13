package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.UserHook;
import com.variant.core.lce.StateParsedLifecycleEvent;
import com.variant.core.schema.State;

/**
 * 
 * @author Igor
 *
 */
public class StateParsedLifecycleEventImpl implements StateParsedLifecycleEvent {

	private State state;
	private ParserResponseImpl response;
	
	StateParsedLifecycleEventImpl(State state, ParserResponseImpl response) {
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
		response.addMessage(severity, message);
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
