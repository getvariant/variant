package com.variant.server.api;

import com.variant.core.CommonError;
import com.variant.core.CoreException;

/**
 * Thrown when a user operation requests an unavailable combination of state and test.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class StateNotInstrumentedException extends ServerException.User {
	
	public StateNotInstrumentedException(ServerException.User e) {
		super(CommonError.STATE_NOT_INSTRUMENTED_BY_TEST, e, e.args);
	}

}