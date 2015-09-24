package com.variant.core.schema.impl;

import com.variant.core.flashpoint.StateParsedFlashpoint;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.ParserResponse;

/**
 * 
 * @author Igor
 *
 */
class StateParsedFlashpointImpl implements StateParsedFlashpoint {

	private State state;
	private ParserResponse response;
	
	StateParsedFlashpointImpl(State state, ParserResponse response) {
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
