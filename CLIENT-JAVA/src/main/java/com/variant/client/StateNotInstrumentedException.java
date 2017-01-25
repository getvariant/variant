package com.variant.client;

import com.variant.core.CommonError;
import com.variant.core.CoreException;

/**
 * Thrown when the underlying connection has been closed.
 * 
 * @author Igor Urisman
 * @since 0.7
 */
@SuppressWarnings("serial")
public class StateNotInstrumentedException extends ClientException.User {
	
	public StateNotInstrumentedException(CoreException.User e) {
		super(CommonError.STATE_NOT_INSTRUMENTED_BY_TEST, e, e.args);
	}

}