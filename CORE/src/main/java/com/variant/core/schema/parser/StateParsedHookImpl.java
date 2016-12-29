package com.variant.core.schema.parser;

import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedHook;

/**
 * 
 * @author Igor
 *
 */
class StateParsedHookImpl implements StateParsedHook {

	private State state;
	private ParserResponse response;
	
	StateParsedHookImpl(State state, ParserResponse response) {
		this.state = state;
		this.response = response;
	}
	
	@Override
	public State getState() {
		return state;
	}

	@Override
	public ParserResponse getParserResponse() {
		return response;
	}
	
	@Override
    public void addMessage(String message) {
    	((ParserResponseImpl) response).addMessage(message);
    }
}
