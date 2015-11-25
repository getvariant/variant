package com.variant.web;


import com.variant.core.flashpoint.FlashpointListener;
import com.variant.core.flashpoint.StateParsedFlashpoint;
import com.variant.core.schema.State;
import com.variant.core.schema.parser.Severity;

/**
 * Additional parse checks not implemented by the core library and specific to the Servlet environment.
 * @author Igor
 *
 */
public class StateParsedFlashpointListenerImpl implements FlashpointListener<StateParsedFlashpoint> {

	@Override
	public Class<StateParsedFlashpoint> getFlashpointClass() {
		return StateParsedFlashpoint.class;
	}

	@Override
	public void post(StateParsedFlashpoint flashpoint) {
		State state = flashpoint.getState();
		String path = state.getParameterMap().get("path");
		if (!path.startsWith("/")) {
			flashpoint.getParserResponse().addMessage(
					Severity.ERROR, 
					"Path property [" + path + "] must start with a '/' in State [" + state.getName() + "]");
		}
	}	

}
