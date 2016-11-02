package com.variant.server.hook.impl;

import com.variant.core.xdm.State;
import com.variant.server.ParserResponse;
import com.variant.server.ParserMessage.Severity;
import com.variant.server.hook.StateParsedHook;
import com.variant.server.schema.ParserResponseImpl;

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
