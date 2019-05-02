package com.variant.core.lifecycle.impl;

import com.variant.core.error.ServerError;
import com.variant.core.error.UserError;
import com.variant.core.error.UserError.Severity;
import com.variant.core.lifecycle.VariationParsedLifecycleEvent;
import com.variant.core.schema.Variation;
import com.variant.core.schema.parser.ParserResponse;

/**
 * 
 * @author Igor
 *
 */
public class VariationParsedLifecycleEventImpl implements VariationParsedLifecycleEvent {

	private Variation variation;
	private ParserResponse response;
	
	public VariationParsedLifecycleEventImpl(Variation variation, ParserResponse response) {
		this.variation = variation;
		this.response = response;
	}
	
	//---------------------------------------------------------------------------------------------//
	//                                          PUBLIC                                             //
	//---------------------------------------------------------------------------------------------//

	@Override
	public VariationParsedLifecycleEvent.PostResult newPostResult() {

		return new VariationParsedLifecycleEvent.PostResult() {
			// Nothing
		};
	}

	@Override
	public Variation getVariation() {
		return variation;
	}

	@Override
	public void addMessage(Severity severity, String message) {
		UserError error = null;
		switch (severity) {
		case INFO: error = ServerError.HOOK_USER_MESSAGE_INFO; break;
		case WARN: error = ServerError.HOOK_USER_MESSAGE_WARN; break;
		case ERROR: error = ServerError.HOOK_USER_MESSAGE_ERROR; break;
		case FATAL: error = ServerError.HOOK_USER_MESSAGE_ERROR; break;
		}
		
		response.addMessage(error, message);
	}

}
