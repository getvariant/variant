package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.UserHook;
import com.variant.core.lce.StateParsedLifecycleEvent;
import com.variant.core.schema.ParserResponse;
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
	public UserHook<StateParsedLifecycleEvent> getDefaultHook() {
		
		return new UserHook<StateParsedLifecycleEvent>() {

			@Override
			public Class<StateParsedLifecycleEvent> getLifecycleEventClass() {
				return StateParsedLifecycleEvent.class;
			}

			@Override
			public UserHook.PostResult post(StateParsedLifecycleEvent event) {
					
				return new StateParsedLifecycleEvent.PostResult() {	
					@Override
					public void addMessage(Severity severity, String message) {}
				};
				
			}	
		};
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public ParserResponse getParserResponse() {
    	return response;
    }

}
