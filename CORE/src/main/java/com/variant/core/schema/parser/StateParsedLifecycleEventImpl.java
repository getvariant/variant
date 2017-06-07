package com.variant.core.schema.parser;

import com.variant.core.UserError.Severity;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.State;
import com.variant.core.schema.StateParsedLifecycleEvent;

/**
 * 
 * @author Igor
 *
 */
class StateParsedLifecycleEventImpl implements StateParsedLifecycleEvent {

	private State state;
	private ParserResponse response;
	
	StateParsedLifecycleEventImpl(State state, ParserResponse response) {
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
    public void addMessage(Severity severity, String message) {
    	((ParserResponseImpl) response).addMessage(severity, message);
    }

}
