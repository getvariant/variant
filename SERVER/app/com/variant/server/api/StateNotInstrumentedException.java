package com.variant.server.api;

import com.variant.core.RuntimeError;

/**
 * Thrown when a user operation requests a combination of state and test that is not
 * defined in the schema.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class StateNotInstrumentedException extends ServerException.User {
	
	public StateNotInstrumentedException(ServerException.User e) {
		super(RuntimeError.STATE_NOT_INSTRUMENTED_BY_TEST, e, e.args);
	}

}