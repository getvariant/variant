package com.variant.core.schema.impl;

import com.variant.core.hook.StateParsedHook;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

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

}
