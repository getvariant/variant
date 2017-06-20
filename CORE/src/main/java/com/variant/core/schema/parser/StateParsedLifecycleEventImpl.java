package com.variant.core.schema.parser;

import com.variant.core.UserHook;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedLifecycleEvent;

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
		return null;
	}

	//---------------------------------------------------------------------------------------------//
	//                                        PUBLIC EXT                                           //
	//---------------------------------------------------------------------------------------------//

	public ParserResponse getParserResponse() {
    	return response;
    }

}
