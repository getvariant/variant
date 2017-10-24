package com.variant.client;

import com.variant.core.RuntimeError;
import com.variant.core.CoreException;

/**
 * Thrown when a user operation requests an unavailable combination of state and test.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class StateNotInstrumentedException extends ClientException.User {
	
	public StateNotInstrumentedException(CoreException.User e) {
		super(RuntimeError.STATE_NOT_INSTRUMENTED_BY_TEST, e, e.args);
	}

}