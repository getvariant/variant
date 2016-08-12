package com.variant.core.xdm.impl;

import com.variant.core.hook.StateParsedHook;
import com.variant.core.schema.ParserResponse;
import com.variant.core.schema.ParserMessage.Severity;
import com.variant.core.xdm.State;

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
    public void addMessage(Severity severity, String message) {
    	((ParserResponseImpl) response).addMessage(severity, message);
    }
}
